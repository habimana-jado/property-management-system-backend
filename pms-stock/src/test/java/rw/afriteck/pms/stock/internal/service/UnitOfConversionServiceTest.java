package rw.afriteck.pms.stock.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.internal.enums.EUnitGroup;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;
import rw.afriteck.pms.stock.internal.model.StockItem;
import rw.afriteck.pms.stock.internal.model.UnitOfMeasure;
import rw.afriteck.pms.stock.internal.repository.ItemPackagingRepository;

@ExtendWith(MockitoExtension.class)
class UnitOfConversionServiceTest {

    @Mock
    ItemPackagingRepository packagings;
    @InjectMocks UnitOfConversionService service;

    private Map<String, UnitOfMeasure> units;
    private UnitOfMeasure g, kg, ml, l, pcs, carton, bottle;

    @BeforeEach
    void setUp() {
        g      = unit("G",      EUnitGroup.WEIGHT, "1");
        kg     = unit("KG",     EUnitGroup.WEIGHT, "1000");
        ml     = unit("ML",     EUnitGroup.VOLUME, "1");
        l      = unit("L",      EUnitGroup.VOLUME, "1000");
        pcs    = unit("PCS",    EUnitGroup.COUNT,  "1");
        carton = unit("CARTON", EUnitGroup.COUNT,  "1");   // factor is meaningless: packaging is per item
        bottle = unit("BOTTLE", EUnitGroup.COUNT,  "1");
        units = Map.of("G", g, "KG", kg, "ML", ml, "L", l, "PCS", pcs);
    }

    // ---------- same unit ----------

    @Test
    void sameUnit_returnsTheQuantityWithoutLookingUpPackaging() {
        StockItem soda = item("SODA", pcs);

        BigDecimal result = service.toBase(soda, pcs, new BigDecimal("3"));

        assertThat(result).isEqualByComparingTo("3");
        verifyNoInteractions(packagings);
    }

    // ---------- group conversions (weight / volume) ----------

    @ParameterizedTest(name = "{0} {1} -> base {2} = {3}")
    @CsvSource({
            "25,   KG, G,  25000",   // receipt of rice: 25 kg into an item counted in grams
            "150,  G,  G,  150",     // recipe line already in the base unit
            "0.15, KG, G,  150",     // same recipe line written in kg
            "0.5,  L,  ML, 500",
            "500,  ML, L,  0.5",     // smaller unit into a larger base unit
            "1,    L,  L,  1"
    })
    void weightAndVolume_convertThroughGroupFactors(String entered, String enteredUnit,
                                                    String baseUnit, String expected) {
        StockItem item = item("ANY", units.get(baseUnit));

        BigDecimal result = service.toBase(item, units.get(enteredUnit), new BigDecimal(entered));

        assertThat(result).isEqualByComparingTo(expected);
    }

    // ---------- item packaging ----------

    @Test
    void packaging_convertsCartonsToPieces() {                       // example A
        StockItem soda = item("SODA", pcs);
        when(packagings.findByItemIdAndUnitId(soda.getId(), carton.getId()))
                .thenReturn(Optional.of(packaging(soda, carton, "24")));

        BigDecimal result = service.toBase(soda, carton, new BigDecimal("5"));

        assertThat(result).isEqualByComparingTo("120");
    }

    @Test
    void packaging_convertsBottlesToMillilitres() {                  // example C
        StockItem whisky = item("WHISKY", ml);
        when(packagings.findByItemIdAndUnitId(whisky.getId(), bottle.getId()))
                .thenReturn(Optional.of(packaging(whisky, bottle, "750")));

        assertThat(service.toBase(whisky, bottle, new BigDecimal("12"))).isEqualByComparingTo("9000");
        assertThat(service.toBase(whisky, bottle, BigDecimal.ONE)).isEqualByComparingTo("750");
    }

    @Test
    void packaging_isPerItem_sameUnitDifferentSize() {
        StockItem whisky = item("WHISKY", ml);
        StockItem beer = item("BEER", ml);
        when(packagings.findByItemIdAndUnitId(whisky.getId(), bottle.getId()))
                .thenReturn(Optional.of(packaging(whisky, bottle, "750")));
        when(packagings.findByItemIdAndUnitId(beer.getId(), bottle.getId()))
                .thenReturn(Optional.of(packaging(beer, bottle, "330")));

        assertThat(service.toBase(whisky, bottle, BigDecimal.ONE)).isEqualByComparingTo("750");
        assertThat(service.toBase(beer, bottle, BigDecimal.ONE)).isEqualByComparingTo("330");
    }

    // ---------- rejections ----------

    @Test
    void countUnitWithoutPackaging_isRejected() {
        StockItem soda = item("SODA", pcs);          // no packaging stubbed: repository returns Optional.empty()

        assertThatThrownBy(() -> service.toBase(soda, carton, BigDecimal.ONE))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("CARTON");
    }

    @Test
    void crossGroupConversion_isRejected() {                         // grams to millilitres needs a density
        StockItem oil = item("OIL", ml);

        assertThatThrownBy(() -> service.toBase(oil, g, new BigDecimal("100")))
                .isInstanceOf(StockRuleViolationException.class);
    }

    @Test
    void weightUnitOnCountedItem_isRejected() {
        StockItem soda = item("SODA", pcs);

        assertThatThrownBy(() -> service.toBase(soda, kg, BigDecimal.ONE))
                .isInstanceOf(StockRuleViolationException.class);
    }

    // ---------- precision and rounding ----------

    @Test
    void result_alwaysHasScaleFour() {
        StockItem rice = item("RICE", g);

        assertThat(service.toBase(rice, g, new BigDecimal("150")).scale()).isEqualTo(4);
        assertThat(service.toBase(rice, kg, new BigDecimal("0.15")).scale()).isEqualTo(4);
    }

    @Test
    void roundsHalfUpAtFourDecimals() {
        StockItem rice = item("RICE", kg);           // base kg, entered in grams

        // 0.05 g = 0.00005 kg -> HALF_UP at 4 decimals = 0.0001
        assertThat(service.toBase(rice, g, new BigDecimal("0.05"))).isEqualByComparingTo("0.0001");
    }

    @Test
    void tinyQuantity_roundsToZero_soCallersMustRejectIt() {
        StockItem rice = item("RICE", kg);

        // 0.0004 g = 0.0000004 kg -> 0.0000. The service returns zero;
        // the callers (recipe expansion, document lines) reject a zero quantity.
        assertThat(service.toBase(rice, g, new BigDecimal("0.0004"))).isEqualByComparingTo("0");
    }

    // ---------- fixtures ----------

    private static UnitOfMeasure unit(String code, EUnitGroup group, String factor) {
        UnitOfMeasure u = new UnitOfMeasure();
        u.setCode(code);
        u.setName(code);
        u.setUnitGroup(group);
        u.setFactorToGroupBase(new BigDecimal(factor));
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        return u;
    }

    private static StockItem item(String code, UnitOfMeasure baseUnit) {
        StockItem i = new StockItem();
        i.setCode(code);
        i.setName(code);
        i.setBaseUnit(baseUnit);
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        return i;
    }

    private static ItemPackaging packaging(StockItem item, UnitOfMeasure unit, String qtyInBase) {
        ItemPackaging p = new ItemPackaging();
        p.setItem(item);
        p.setUnit(unit);
        p.setQuantityInBaseUnit(new BigDecimal(qtyInBase));
        return p;
    }
}
