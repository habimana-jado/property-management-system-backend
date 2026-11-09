package rw.afriteck.pms.stock.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.stock.api.command.*;
import rw.afriteck.pms.stock.api.enums.*;
import rw.afriteck.pms.stock.internal.dtos.BalanceKey;
import rw.afriteck.pms.stock.internal.dtos.MovementContext;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.StockMovementRepository;

import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.internal.enums.*;
import rw.afriteck.pms.stock.internal.repository.*;

@ExtendWith(MockitoExtension.class)
class StockApiImplTest {

    private static final UUID WAITER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CASHIER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock OutletStoreResolver storeResolver;
    @Mock StockLedger ledger;
    @Mock StockMovementRepository movements;
    @Mock StockItemRepository items;
    @Mock RecipeRepository recipes;
    @Mock ItemPackagingRepository packagings;

    private StockApiImpl service;

    private final UUID outletId = UUID.randomUUID();
    private final UUID sourceId = UUID.randomUUID();
    private final Map<UUID, StockItem> knownItems = new HashMap<>();
    private final Map<UUID, Recipe> knownRecipes = new HashMap<>();

    private Store store;
    private UnitOfMeasure g, kg, ml, pcs;

    @BeforeEach
    void setUp() {
        service = new StockApiImpl(storeResolver, ledger, movements, items, recipes,
                new UnitOfConversionService(packagings));

        g   = unit("G",   EUnitGroup.WEIGHT, "1");
        kg  = unit("KG",  EUnitGroup.WEIGHT, "1000");
        ml  = unit("ML",  EUnitGroup.VOLUME, "1");
        pcs = unit("PCS", EUnitGroup.COUNT,  "1");

        store = new Store();
        store.setName("Terrace Kitchen");
        ReflectionTestUtils.setField(store, "id", UUID.randomUUID());

        // lenient: not every test reaches every collaborator
        lenient().when(storeResolver.resolveIssuingStore(any(), any(), any())).thenReturn(store);

        // bulk loads: answer from the fixtures; unknown ids are simply absent, like the real repositories
        lenient().when(items.findAllById(any())).thenAnswer(inv -> {
            Iterable<UUID> ids = inv.getArgument(0);
            List<StockItem> found = new ArrayList<>();
            for (UUID id : ids) {
                StockItem i = knownItems.get(id);
                if (i != null) found.add(i);
            }
            return found;
        });
        lenient().when(recipes.findWithLinesByIdIn(any())).thenAnswer(inv -> {
            Collection<UUID> ids = inv.getArgument(0);
            List<Recipe> found = new ArrayList<>();
            for (UUID id : ids) {
                Recipe r = knownRecipes.get(id);
                if (r != null) found.add(r);
            }
            return found;
        });

        // lockAll: hand back one balance per distinct key, like the real ledger would
        lenient().when(ledger.lockAll(anyCollection())).thenAnswer(inv -> {
            Collection<BalanceKey> keys = inv.getArgument(0);
            Map<BalanceKey, StockBalance> map = new LinkedHashMap<>();
            for (BalanceKey k : keys) {
                map.computeIfAbsent(k, key -> balance(knownItems.get(key.itemId())));
            }
            return map;
        });

        // post: return a movement with an id
        lenient().when(ledger.post(any(), any(), any(), any())).thenAnswer(inv -> {
            StockMovement m = new StockMovement();
            ReflectionTestUtils.setField(m, "id", UUID.randomUUID());
            return m;
        });
    }

    // ================= consumeForSale: direct items =================

    @Test
    void directItem_postsOneNegativeSaleMovement() {
        StockItem soda = item("SODA", pcs);
        UUID lineId = UUID.randomUUID();

        List<UUID> ids = service.consumeForSale(command(
                new SaleLine(lineId, soda.getId(), null, new BigDecimal("2"), true)));

        assertThat(ids).hasSize(1);
        Posted p = verifyPosted(1).get(0);
        assertThat(p.itemCode()).isEqualTo("SODA");
        assertThat(p.type()).isEqualTo(EMovementType.SALE);
        assertThat(p.delta()).isEqualByComparingTo("-2");
    }

    @Test
    void untrackedLines_areSkipped_andNothingIsLocked() {
        StockItem soda = item("SODA", pcs);

        List<UUID> ids = service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, false)));

        assertThat(ids).isEmpty();
        verifyNoInteractions(ledger);
    }

    @Test
    void anOrderWithNothingTracked_doesNotEvenResolveTheStoreOrLoadAnything() {
        StockItem soda = item("SODA", pcs);
        Recipe burger = recipe("Burger", true, line(item("SAUCE", ml), ml, "20"));

        List<UUID> ids = service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, false),
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, false)));

        assertThat(ids).isEmpty();
        verifyNoInteractions(storeResolver, items, recipes, ledger);
    }

    @Test
    void mixedLines_onlyTrackedOnesArePosted() {
        StockItem soda = item("SODA", pcs);
        StockItem water = item("WATER", pcs);

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), water.getId(), null, BigDecimal.ONE, false)));

        List<Posted> posted = verifyPosted(1);
        assertThat(posted.get(0).itemCode()).isEqualTo("SODA");
    }

    // ================= consumeForSale: recipes =================

    @Test
    void recipe_expandsIntoOneMovementPerIngredient_convertingUnits() {   // example E
        StockItem bun = item("BUN", pcs);
        StockItem patty = item("PATTY", g);
        StockItem cheese = item("CHEESE", pcs);
        StockItem sauce = item("SAUCE", ml);
        Recipe burger = recipe("Burger", true,
                line(bun, pcs, "1"),
                line(patty, kg, "0.15"),        // 0.15 kg of a gram-based item = 150 g
                line(cheese, pcs, "1"),
                line(sauce, ml, "20"));

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, burger.getId(), new BigDecimal("2"), true)));

        Map<String, BigDecimal> deltas = new HashMap<>();
        verifyPosted(4).forEach(p -> deltas.put(p.itemCode(), p.delta()));
        assertThat(deltas.get("BUN")).isEqualByComparingTo("-2");
        assertThat(deltas.get("PATTY")).isEqualByComparingTo("-300");
        assertThat(deltas.get("CHEESE")).isEqualByComparingTo("-2");
        assertThat(deltas.get("SAUCE")).isEqualByComparingTo("-40");
    }

    @Test
    void twoLinesSharingAnIngredient_postOneMovementPerLine() {
        StockItem sauce = item("SAUCE", ml);
        Recipe burger = recipe("Burger", true, line(sauce, ml, "20"));
        Recipe wrap = recipe("Wrap", true, line(sauce, ml, "30"));
        UUID burgerLine = UUID.randomUUID();
        UUID wrapLine = UUID.randomUUID();

        service.consumeForSale(command(
                new SaleLine(burgerLine, null, burger.getId(), BigDecimal.ONE, true),
                new SaleLine(wrapLine, null, wrap.getId(), BigDecimal.ONE, true)));

        List<Posted> posted = verifyPosted(2);
        assertThat(posted).extracting(Posted::itemCode).containsOnly("SAUCE");
        assertThat(posted).extracting(p -> p.ctx().sourceLineId())
                .containsExactlyInAnyOrder(burgerLine, wrapLine);   // reversible line by line
    }

    // ================= consumeForSale: bulk loading =================

    @Test
    void aWholeOrder_loadsItemsAndRecipesWithOneQueryEach() {
        StockItem soda = item("SODA", pcs);
        StockItem water = item("WATER", pcs);
        StockItem sauce = item("SAUCE", ml);
        Recipe burger = recipe("Burger", true, line(sauce, ml, "20"));
        Recipe wrap = recipe("Wrap", true, line(sauce, ml, "30"));

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), water.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), null, wrap.getId(), BigDecimal.ONE, true)));

        verify(items, times(1)).findAllById(any());
        verify(recipes, times(1)).findWithLinesByIdIn(any());
        verify(items, never()).findById(any());
        verify(recipes, never()).findWithLinesById(any());
    }

    @Test
    void bulkLoad_requestsEachDistinctIdOnce_andOnlyForTrackedLines() {
        StockItem soda = item("SODA", pcs);
        StockItem water = item("WATER", pcs);
        Recipe burger = recipe("Burger", true, line(item("SAUCE", ml), ml, "20"));

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),      // same item twice
                new SaleLine(UUID.randomUUID(), water.getId(), null, BigDecimal.ONE, false),    // untracked: not loaded
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, true)));  // same recipe twice

        ArgumentCaptor<Iterable<UUID>> itemIds = ArgumentCaptor.forClass(Iterable.class);
        verify(items).findAllById(itemIds.capture());
        assertThat(itemIds.getValue()).containsExactly(soda.getId());

        ArgumentCaptor<Collection<UUID>> recipeIds = ArgumentCaptor.forClass(Collection.class);
        verify(recipes).findWithLinesByIdIn(recipeIds.capture());
        assertThat(recipeIds.getValue()).containsExactly(burger.getId());
    }

    @Test
    void anOrderOfOnlyDirectItems_neverQueriesRecipes() {
        StockItem soda = item("SODA", pcs);

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true)));

        verify(items).findAllById(any());
        verifyNoInteractions(recipes);
    }

    @Test
    void anOrderOfOnlyRecipes_neverQueriesItemsDirectly() {
        Recipe burger = recipe("Burger", true, line(item("SAUCE", ml), ml, "20"));

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, true)));

        verify(recipes).findWithLinesByIdIn(any());
        verifyNoInteractions(items);
    }

    // ================= consumeForSale: bookkeeping =================

    @Test
    void everyMovementCarriesTheSourceReference() {
        StockItem soda = item("SODA", pcs);
        UUID lineId = UUID.randomUUID();

        service.consumeForSale(command(new SaleLine(lineId, soda.getId(), null, BigDecimal.ONE, true)));

        MovementContext ctx = verifyPosted(1).get(0).ctx();
        assertThat(ctx.sourceType()).isEqualTo(EMovementSourceType.TABLE_BILL_LINE);
        assertThat(ctx.sourceId()).isEqualTo(sourceId);
        assertThat(ctx.sourceLineId()).isEqualTo(lineId);
        assertThat(ctx.performedBy()).isEqualTo(WAITER_ID);
    }

    @Test
    void balancesAreLockedBeforeAnyMovementIsPosted() {
        StockItem soda = item("SODA", pcs);
        StockItem water = item("WATER", pcs);

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), water.getId(), null, BigDecimal.ONE, true)));

        InOrder order = inOrder(ledger);
        order.verify(ledger).lockAll(anyCollection());
        order.verify(ledger, times(2)).post(any(), any(), any(), any());
    }

    @Test
    void locksTheStoreAndItemOfEveryRequirement() {
        StockItem soda = item("SODA", pcs);

        service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true)));

        ArgumentCaptor<Collection<BalanceKey>> keys = ArgumentCaptor.forClass(Collection.class);
        verify(ledger).lockAll(keys.capture());
        assertThat(keys.getValue()).containsExactly(new BalanceKey(store.getId(), soda.getId()));
    }

    @Test
    void resolvesTheStoreFromTheOutlet() {
        StockItem soda = item("SODA", pcs);
        UUID requestedStore = UUID.randomUUID();

        service.consumeForSale(new ConsumeForSaleCommand(EOutletType.POS_TERMINAL, outletId, requestedStore,
                EMovementSourceType.POS_SALE_LINE, sourceId,
                List.of(new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true)), CASHIER_ID));

        verify(storeResolver).resolveIssuingStore(EOutletType.POS_TERMINAL, outletId, requestedStore);
    }

    // ================= consumeForSale: rejections =================

    @Test
    void bothItemAndRecipe_isRejected_beforeAnyLookup() {
        StockItem soda = item("SODA", pcs);
        Recipe burger = recipe("Burger", true, line(soda, pcs, "1"));

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), burger.getId(), BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("exactly one");
        verifyNoInteractions(storeResolver, items, recipes, ledger);
    }

    @Test
    void neitherItemNorRecipe_isRejected_beforeAnyLookup() {
        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, null, BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class);
        verifyNoInteractions(storeResolver, items, recipes, ledger);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void nonPositiveQuantity_isRejected_beforeAnyLookup(String quantity) {
        StockItem soda = item("SODA", pcs);

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, new BigDecimal(quantity), true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("positive");
        verifyNoInteractions(storeResolver, items, recipes, ledger);
    }

    @Test
    void aMalformedLine_rejectsTheWholeOrder_evenIfAnEarlierLineIsFine() {
        StockItem soda = item("SODA", pcs);

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), null, null, BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class);
        verifyNoInteractions(storeResolver, items, recipes, ledger);          // nothing was even loaded
    }

    @Test
    void unknownItem_isRejected_andNothingIsLocked() {
        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), UUID.randomUUID(), null, BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown stock item");
        verifyNothingLockedOrPosted();
    }

    @Test
    void inactiveItem_isRejected() {
        StockItem soda = item("SODA", pcs);
        soda.setActive(false);

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("inactive");
        verifyNothingLockedOrPosted();
    }

    @Test
    void unknownRecipe_isRejected() {
        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, UUID.randomUUID(), BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown recipe");
        verifyNothingLockedOrPosted();
    }

    @Test
    void inactiveRecipe_isRejected() {
        StockItem sauce = item("SAUCE", ml);
        Recipe burger = recipe("Burger", false, line(sauce, ml, "20"));

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("inactive");
        verifyNothingLockedOrPosted();
    }

    @Test
    void recipeWithAnInactiveIngredient_isRejected() {
        StockItem sauce = item("SAUCE", ml);
        sauce.setActive(false);
        Recipe burger = recipe("Burger", true, line(sauce, ml, "20"));

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, burger.getId(), BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("inactive");
        verifyNothingLockedOrPosted();
    }

    @Test
    void recipeQuantityThatRoundsToZero_isRejected() {
        StockItem salt = item("SALT", kg);                       // counted in kg
        Recipe dish = recipe("Dish", true, line(salt, g, "0.0004"));   // 0.0004 g -> 0.0000 kg

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), null, dish.getId(), BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("rounds to zero");
        verifyNothingLockedOrPosted();
    }

    @Test
    void oneUnknownLine_rejectsTheWholeOrder_beforeAnythingIsLocked() {
        StockItem soda = item("SODA", pcs);

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.ONE, true),
                new SaleLine(UUID.randomUUID(), UUID.randomUUID(), null, BigDecimal.ONE, true))))
                .isInstanceOf(StockRuleViolationException.class);
        verifyNothingLockedOrPosted();                            // no partial work
    }

    @Test
    void insufficientStock_fromTheLedger_propagatesToTheCaller() {
        StockItem soda = item("SODA", pcs);
        when(ledger.post(any(), any(), any(), any())).thenThrow(
                new InsufficientStockException("Terrace Kitchen", "SODA", BigDecimal.ONE, BigDecimal.TEN));

        assertThatThrownBy(() -> service.consumeForSale(command(
                new SaleLine(UUID.randomUUID(), soda.getId(), null, BigDecimal.TEN, true))))
                .isInstanceOf(InsufficientStockException.class);   // the caller's transaction rolls back
    }

    // ================= reverseConsumption =================

    @Test
    void reverse_withNothingToReverse_returnsZeroAndTouchesNoBalance() {
        int count = service.reverseConsumption(new ReverseConsumptionCommand(
                EMovementSourceType.TABLE_BILL_LINE, sourceId, null, "Line voided", WAITER_ID));

        assertThat(count).isZero();
        verifyNoInteractions(ledger);
    }

    @Test
    void reverse_locksEveryAffectedBalance_andReversesEachMovement() {
        StockItem soda = item("SODA", pcs);
        StockItem water = item("WATER", pcs);
        StockMovement m1 = movement(soda);
        StockMovement m2 = movement(water);
        when(movements.findReversible(EMovementSourceType.TABLE_BILL_LINE, sourceId, null))
                .thenReturn(List.of(m1, m2));

        int count = service.reverseConsumption(new ReverseConsumptionCommand(
                EMovementSourceType.TABLE_BILL_LINE, sourceId, null, "Bill voided", MANAGER_ID));

        assertThat(count).isEqualTo(2);
        ArgumentCaptor<StockMovement> originals = ArgumentCaptor.forClass(StockMovement.class);
        ArgumentCaptor<StockBalance> balances = ArgumentCaptor.forClass(StockBalance.class);
        InOrder order = inOrder(ledger);
        order.verify(ledger).lockAll(anyCollection());
        order.verify(ledger, times(2)).reverse(balances.capture(), originals.capture(),
                eq("Bill voided"), eq(MANAGER_ID));
        assertThat(originals.getAllValues()).containsExactly(m1, m2);
        assertThat(balances.getAllValues()).extracting(b -> b.getItem().getCode())
                .containsExactly("SODA", "WATER");               // each reversal hits its own item's balance
    }

    @Test
    void reverse_passesTheLineFilterToTheRepository() {
        UUID lineId = UUID.randomUUID();

        service.reverseConsumption(new ReverseConsumptionCommand(
                EMovementSourceType.TABLE_BILL_LINE, sourceId, lineId, "Line voided", WAITER_ID));

        verify(movements).findReversible(EMovementSourceType.TABLE_BILL_LINE, sourceId, lineId);
    }

    // ================= fixtures and helpers =================

    private ConsumeForSaleCommand command(SaleLine... lines) {
        return new ConsumeForSaleCommand(EOutletType.RESTAURANT, outletId, null,
                EMovementSourceType.TABLE_BILL_LINE, sourceId, List.of(lines), WAITER_ID);
    }

    private record Posted(String itemCode, EMovementType type, BigDecimal delta, MovementContext ctx) {}

    private List<Posted> verifyPosted(int expectedCount) {
        ArgumentCaptor<StockBalance> balance = ArgumentCaptor.forClass(StockBalance.class);
        ArgumentCaptor<EMovementType> type = ArgumentCaptor.forClass(EMovementType.class);
        ArgumentCaptor<BigDecimal> delta = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<MovementContext> ctx = ArgumentCaptor.forClass(MovementContext.class);
        verify(ledger, times(expectedCount))
                .post(balance.capture(), type.capture(), delta.capture(), ctx.capture());
        List<Posted> result = new ArrayList<>();
        for (int i = 0; i < expectedCount; i++) {
            result.add(new Posted(balance.getAllValues().get(i).getItem().getCode(),
                    type.getAllValues().get(i), delta.getAllValues().get(i), ctx.getAllValues().get(i)));
        }
        return result;
    }

    private void verifyNothingLockedOrPosted() {
        verify(ledger, never()).lockAll(anyCollection());
        verify(ledger, never()).post(any(), any(), any(), any());
    }

    private StockBalance balance(StockItem item) {
        StockBalance b = new StockBalance();
        b.setStore(store);
        b.setItem(item);
        b.setQuantityOnHand(new BigDecimal("1000"));
        return b;
    }

    private StockMovement movement(StockItem item) {
        StockMovement m = new StockMovement();
        m.setStore(store);
        m.setItem(item);
        m.setMovementType(EMovementType.SALE);
        m.setQuantity(BigDecimal.ONE.negate());
        ReflectionTestUtils.setField(m, "id", UUID.randomUUID());
        return m;
    }

    private static UnitOfMeasure unit(String code, EUnitGroup group, String factor) {
        UnitOfMeasure u = new UnitOfMeasure();
        u.setCode(code);
        u.setName(code);
        u.setUnitGroup(group);
        u.setFactorToGroupBase(new BigDecimal(factor));
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        return u;
    }

    private StockItem item(String code, UnitOfMeasure baseUnit) {
        StockItem i = new StockItem();
        i.setCode(code);
        i.setName(code);
        i.setBaseUnit(baseUnit);
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        knownItems.put(i.getId(), i);                            // the bulk-load stub answers from this map
        return i;
    }

    private static RecipeLine line(StockItem ingredient, UnitOfMeasure unit, String quantity) {
        RecipeLine l = new RecipeLine();
        l.setIngredient(ingredient);
        l.setUnit(unit);
        l.setQuantity(new BigDecimal(quantity));
        return l;
    }

    private Recipe recipe(String name, boolean active, RecipeLine... lines) {
        Recipe r = new Recipe();
        r.setName(name);
        r.setActive(active);
        Arrays.stream(lines).forEach(r::addLine);
        ReflectionTestUtils.setField(r, "id", UUID.randomUUID());
        knownRecipes.put(r.getId(), r);                          // the bulk-load stub answers from this map
        return r;
    }
}