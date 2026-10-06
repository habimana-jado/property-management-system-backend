package rw.afriteck.pms.stock.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.internal.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.internal.enums.EUnitGroup;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.*;

@ExtendWith(MockitoExtension.class)
class StocktakeServiceImplTest {

    @Mock
    StockTakeRepository stocktakes;
    @Mock
    StoreRepository stores;
    @Mock
    StockItemRepository items;
    @Mock
    UnitOfMeasureRepository units;
    @Mock
    StockBalanceRepository balances;
    @Mock
    StockDocumentRepository documents;
    @Mock
    ItemPackagingRepository packagings;
    @Mock
    StockOperations operations;

    private StocktakeServiceImpl service;

    private Store store;
    private UnitOfMeasure pcs, g, kg;
    private StockItem soda, water, juice;

    @BeforeEach
    void setUp() {
        service = new StocktakeServiceImpl(stocktakes, stores, items, units, balances, documents,
                new UnitOfConversionService(packagings), operations);

        store = store();
        pcs = unit("PCS", EUnitGroup.COUNT,  "1");
        g   = unit("G",   EUnitGroup.WEIGHT, "1");
        kg  = unit("KG",  EUnitGroup.WEIGHT, "1000");
        soda  = item("SODA",  pcs);
        water = item("WATER", pcs);
        juice = item("JUICE", pcs);

        // save assigns an id, like JPA would on persist
        lenient().when(stocktakes.save(any(Stocktake.class))).thenAnswer(inv -> {
            Stocktake s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
            return s;
        });
    }

    // =====================================================================
    // start
    // =====================================================================

    @Test
    void start_createsACountingStocktake_withOneLinePerExistingBalance() {
        when(balances.findByStoreId(store.getId()))
                .thenReturn(List.of(balance(soda, "10"), balance(water, "4")));

        UUID id = service.start(store.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        Stocktake st = savedStocktake();
        assertThat(id).isEqualTo(st.getId());
        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.COUNTING);
        assertThat(st.getStore()).isSameAs(store);
        assertThat(st.getStartedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(st.getStartedAt()).isNotNull();
        assertThat(st.getApprovedAt()).isNull();
        assertThat(st.getAdjustmentDocument()).isNull();

        assertThat(st.getLines()).hasSize(2);
        assertThat(st.getLines()).extracting(l -> l.getItem().getCode()).containsExactly("SODA", "WATER");
        assertThat(st.getLines().get(0).getExpectedQuantity()).isEqualByComparingTo("10");
        assertThat(st.getLines().get(1).getExpectedQuantity()).isEqualByComparingTo("4");
        assertThat(st.getLines()).allSatisfy(l -> {
            assertThat(l.getCountedQuantity()).isNull();
            assertThat(l.getStocktake()).isSameAs(st);
        });
    }

    @Test
    void start_onAStoreWithNoBalances_stillCreatesTheStocktake() {
        service.start(store.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(savedStocktake().getLines()).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void start_refusesWhenAnyOpenStocktakeExists_checkingAllThreeOpenStatuses() {
        when(stocktakes.existsByStoreIdAndStatusIn(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.start(store.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("already has an open stocktake");

        ArgumentCaptor<Collection<EStocktakeStatus>> statuses = ArgumentCaptor.forClass(Collection.class);
        verify(stocktakes).existsByStoreIdAndStatusIn(org.mockito.ArgumentMatchers.eq(store.getId()), statuses.capture());
        assertThat(statuses.getValue()).containsExactlyInAnyOrder(
                EStocktakeStatus.DRAFT, EStocktakeStatus.COUNTING, EStocktakeStatus.SUBMITTED);
        verify(stocktakes, never()).save(any());
    }

    @Test
    void start_onAnUnknownStore_isRejected() {
        assertThatThrownBy(() -> service.start(UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown store");

        verify(stocktakes, never()).save(any());
    }

    // =====================================================================
    // recordCount
    // =====================================================================

    @Test
    void recordCount_storesTheCount_andRefreshesExpectedFromTheLiveBalance() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "10", null));
        when(balances.findByStoreIdAndItemId(store.getId(), soda.getId()))
                .thenReturn(Optional.of(balance(soda, "8")));        // 2 were sold since the count started

        service.recordCount(st.getId(), soda.getId(), new BigDecimal("7"), pcs.getId());

        StocktakeLine line = st.getLines().getFirst();
        assertThat(line.getExpectedQuantity()).isEqualByComparingTo("8");     // not the stale 10
        assertThat(line.getCountedQuantity()).isEqualByComparingTo("7");
        assertThat(line.getVariance()).isEqualByComparingTo("-1");
    }

    @Test
    void recordCount_convertsTheEnteredUnitToTheItemsBaseUnit() {
        StockItem rice = item("RICE", g);
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(rice, "5000", null));
        when(balances.findByStoreIdAndItemId(store.getId(), rice.getId()))
                .thenReturn(Optional.of(balance(rice, "5000")));

        service.recordCount(st.getId(), rice.getId(), new BigDecimal("2"), kg.getId());   // 2 kg of a gram item

        assertThat(st.getLines().getFirst().getCountedQuantity()).isEqualByComparingTo("2000");
    }

    @Test
    void recordCount_acceptsAZeroCount() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "3", null));
        when(balances.findByStoreIdAndItemId(store.getId(), soda.getId()))
                .thenReturn(Optional.of(balance(soda, "3")));

        service.recordCount(st.getId(), soda.getId(), BigDecimal.ZERO, pcs.getId());

        assertThat(st.getLines().getFirst().getCountedQuantity()).isEqualByComparingTo("0");
        assertThat(st.getLines().getFirst().getVariance()).isEqualByComparingTo("-3");
    }

    @Test
    void recordCount_forAnItemNotOnTheSheet_addsALine_expectingTheLiveBalance() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "10", null));
        when(balances.findByStoreIdAndItemId(store.getId(), water.getId()))
                .thenReturn(Optional.of(balance(water, "2")));

        service.recordCount(st.getId(), water.getId(), new BigDecimal("5"), pcs.getId());

        assertThat(st.getLines()).hasSize(2);
        StocktakeLine added = st.getLines().get(1);
        assertThat(added.getItem()).isSameAs(water);
        assertThat(added.getStocktake()).isSameAs(st);
        assertThat(added.getExpectedQuantity()).isEqualByComparingTo("2");
        assertThat(added.getCountedQuantity()).isEqualByComparingTo("5");
    }

    @Test
    void recordCount_forStockFoundWithNoBalanceRow_expectsZero_soTheVarianceIsTheWholeCount() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING);       // balances repo returns Optional.empty()

        service.recordCount(st.getId(), juice.getId(), new BigDecimal("5"), pcs.getId());

        StocktakeLine added = st.getLines().getFirst();
        assertThat(added.getExpectedQuantity()).isEqualByComparingTo("0");
        assertThat(added.getVariance()).isEqualByComparingTo("5");
    }

    @Test
    void recordCount_twiceForTheSameItem_updatesTheSameLine() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "10", null));
        when(balances.findByStoreIdAndItemId(store.getId(), soda.getId()))
                .thenReturn(Optional.of(balance(soda, "8")), Optional.of(balance(soda, "6")));

        service.recordCount(st.getId(), soda.getId(), new BigDecimal("7"), pcs.getId());
        service.recordCount(st.getId(), soda.getId(), new BigDecimal("6"), pcs.getId());   // recount

        assertThat(st.getLines()).hasSize(1);
        assertThat(st.getLines().getFirst().getExpectedQuantity()).isEqualByComparingTo("6");
        assertThat(st.getLines().getFirst().getCountedQuantity()).isEqualByComparingTo("6");
        assertThat(st.getLines().getFirst().getVariance()).isEqualByComparingTo("0");
    }

    @ParameterizedTest
    @EnumSource(value = EStocktakeStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "COUNTING")
    void recordCount_isRejectedUnlessTheStocktakeIsCounting(EStocktakeStatus status) {
        Stocktake st = stocktake(status, line(soda, "10", null));

        assertThatThrownBy(() -> service.recordCount(st.getId(), soda.getId(), BigDecimal.ONE, pcs.getId()))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("not in counting state");

        assertThat(st.getLines().getFirst().getCountedQuantity()).isNull();
    }

    @Test
    void recordCount_rejectsUnknownStocktake_item_andUnit() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING);

        assertThatThrownBy(() -> service.recordCount(UUID.randomUUID(), soda.getId(), BigDecimal.ONE, pcs.getId()))
                .hasMessageContaining("Unknown stocktake");
        assertThatThrownBy(() -> service.recordCount(st.getId(), UUID.randomUUID(), BigDecimal.ONE, pcs.getId()))
                .hasMessageContaining("Unknown item");
        assertThatThrownBy(() -> service.recordCount(st.getId(), soda.getId(), BigDecimal.ONE, UUID.randomUUID()))
                .hasMessageContaining("Unknown unit");

        assertThat(st.getLines()).isEmpty();
    }

    @Test
    void recordCount_rejectsANegativeCount() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "10", null));

        assertThatThrownBy(() -> service.recordCount(st.getId(), soda.getId(), new BigDecimal("-1"), pcs.getId()))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("cannot be negative");

        assertThat(st.getLines().getFirst().getCountedQuantity()).isNull();
    }

    // =====================================================================
    // submit
    // =====================================================================

    @Test
    void submit_movesACountingStocktakeWithCountsToSubmitted() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "10", "9"), line(water, "4", null));

        service.submit(st.getId());

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.SUBMITTED);
    }

    @Test
    void submit_withNothingCounted_isRejected_andStaysCounting() {
        Stocktake st = stocktake(EStocktakeStatus.COUNTING, line(soda, "10", null));

        assertThatThrownBy(() -> service.submit(st.getId()))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Nothing has been counted");

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.COUNTING);
    }

    @ParameterizedTest
    @EnumSource(value = EStocktakeStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "COUNTING")
    void submit_isRejectedUnlessTheStocktakeIsCounting(EStocktakeStatus status) {
        Stocktake st = stocktake(status, line(soda, "10", "9"));

        assertThatThrownBy(() -> service.submit(st.getId()))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("not in counting state");

        assertThat(st.getStatus()).isEqualTo(status);
    }

    @Test
    void submit_onAnUnknownStocktake_isRejected() {
        assertThatThrownBy(() -> service.submit(UUID.randomUUID()))
                .hasMessageContaining("Unknown stocktake");
    }

    // =====================================================================
    // approve
    // =====================================================================

    @Test
    void approve_postsOneAdjustmentWithTheVariances_andClosesTheStocktake() {
        Stocktake st = stocktake(EStocktakeStatus.SUBMITTED,
                line(soda, "50", "47"),      // -3
                line(water, "10", "12"));    // +2
        UUID docId = UUID.randomUUID();
        StockDocument documentRef = new StockDocument();
        when(operations.postDocument(any())).thenReturn(docId);
        when(documents.getReferenceById(docId)).thenReturn(documentRef);

        UUID result = service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(result).isEqualTo(docId);

        SingleStoreDocumentCommand cmd = postedCommand();
        assertThat(cmd.type()).isEqualTo(EDocumentType.ADJUSTMENT);
        assertThat(cmd.storeId()).isEqualTo(store.getId());
        assertThat(cmd.supplierName()).isNull();
        assertThat(cmd.externalReference()).isEqualTo("STOCKTAKE:" + st.getId());
        assertThat(cmd.note()).isEqualTo("Stocktake approval");
        assertThat(cmd.performedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(cmd.lines()).hasSize(2);
        assertThat(cmd.lines().getFirst().itemId()).isEqualTo(soda.getId());
        assertThat(cmd.lines().get(0).quantity()).isEqualByComparingTo("-3");
        assertThat(cmd.lines().get(0).unitId()).isEqualTo(pcs.getId());      // the item's base unit
        assertThat(cmd.lines().get(1).itemId()).isEqualTo(water.getId());
        assertThat(cmd.lines().get(1).quantity()).isEqualByComparingTo("2");

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.APPROVED);
        assertThat(st.getApprovedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(st.getApprovedAt()).isNotNull();
        assertThat(st.getAdjustmentDocument()).isSameAs(documentRef);
    }

    @Test
    void approve_leavesOutZeroVarianceAndUncountedLines() {
        Stocktake st = stocktake(EStocktakeStatus.SUBMITTED,
                line(soda, "50", "47"),      // -3: adjusted
                line(water, "10", "10"),     // 0: nothing to correct
                line(juice, "5", null));     // never counted: unknown, so no adjustment
        when(operations.postDocument(any())).thenReturn(UUID.randomUUID());

        service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(postedCommand().lines()).extracting(DocumentLineInput::itemId)
                .containsExactly(soda.getId());
    }

    @Test
    void approve_withNoVariance_approvesWithoutPostingAnything() {
        Stocktake st = stocktake(EStocktakeStatus.SUBMITTED,
                line(soda, "50", "50"),
                line(water, "10", null));

        UUID result = service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(result).isNull();
        verifyNoInteractions(operations);
        verify(documents, never()).getReferenceById(any());
        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.APPROVED);
        assertThat(st.getAdjustmentDocument()).isNull();
        assertThat(st.getApprovedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(st.getApprovedAt()).isNotNull();
    }

    @Test
    void approve_locksTheStocktakeRow_insteadOfAPlainRead() {
        Stocktake st = stocktake(EStocktakeStatus.SUBMITTED, line(soda, "50", "50"));

        service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        verify(stocktakes).findForUpdate(st.getId());
        verify(stocktakes, never()).findById(any());
    }

    @Test
    void approve_aSecondTime_isRejected_andPostsNothingMore() {
        Stocktake st = stocktake(EStocktakeStatus.SUBMITTED, line(soda, "50", "47"));
        when(operations.postDocument(any())).thenReturn(UUID.randomUUID());

        service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThatThrownBy(() -> service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000002")))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("not submitted");
        verify(operations, org.mockito.Mockito.times(1)).postDocument(any());
        assertThat(st.getApprovedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    @ParameterizedTest
    @EnumSource(value = EStocktakeStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "SUBMITTED")
    void approve_isRejectedUnlessTheStocktakeIsSubmitted(EStocktakeStatus status) {
        Stocktake st = stocktake(status, line(soda, "50", "47"));

        assertThatThrownBy(() -> service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("not submitted");

        verifyNoInteractions(operations);
        assertThat(st.getStatus()).isEqualTo(status);
    }

    @Test
    void approve_onAnUnknownStocktake_isRejected() {
        assertThatThrownBy(() -> service.approve(UUID.randomUUID(), UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .hasMessageContaining("Unknown stocktake");
    }

    @Test
    void approve_whenThePostingFails_theStocktakeIsNotMarkedApproved() {
        Stocktake st = stocktake(EStocktakeStatus.SUBMITTED, line(soda, "50", "47"));
        when(operations.postDocument(any())).thenThrow(new StockRuleViolationException("Posting failed"));

        assertThatThrownBy(() -> service.approve(st.getId(), UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .isInstanceOf(StockRuleViolationException.class);

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.SUBMITTED);
        assertThat(st.getApprovedBy()).isNull();
        assertThat(st.getAdjustmentDocument()).isNull();
    }

    // =====================================================================
    // cancel
    // =====================================================================

    @ParameterizedTest
    @EnumSource(value = EStocktakeStatus.class, mode = EnumSource.Mode.EXCLUDE, names = "APPROVED")
    void cancel_worksOnAnyStatusExceptApproved(EStocktakeStatus status) {
        Stocktake st = stocktake(status);

        service.cancel(st.getId());

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.CANCELLED);
    }

    @Test
    void cancel_anApprovedStocktake_isRejected_andStaysApproved() {
        Stocktake st = stocktake(EStocktakeStatus.APPROVED);

        assertThatThrownBy(() -> service.cancel(st.getId()))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("approved stocktake cannot be cancelled");

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.APPROVED);
    }

    @Test
    void cancel_onAnUnknownStocktake_isRejected() {
        assertThatThrownBy(() -> service.cancel(UUID.randomUUID()))
                .hasMessageContaining("Unknown stocktake");
    }

    // =====================================================================
    // fixtures and helpers
    // =====================================================================

    private Stocktake savedStocktake() {
        ArgumentCaptor<Stocktake> captor = ArgumentCaptor.forClass(Stocktake.class);
        verify(stocktakes).save(captor.capture());
        return captor.getValue();
    }

    private SingleStoreDocumentCommand postedCommand() {
        ArgumentCaptor<SingleStoreDocumentCommand> captor = ArgumentCaptor.forClass(SingleStoreDocumentCommand.class);
        verify(operations).postDocument(captor.capture());
        return captor.getValue();
    }

    /** A stocktake on the test store, registered for both the plain and the locking lookup. */
    private Stocktake stocktake(EStocktakeStatus status, StocktakeLine... lines) {
        Stocktake st = new Stocktake();
        st.setStore(store);
        st.setStatus(status);
        Arrays.stream(lines).forEach(st::addLine);
        ReflectionTestUtils.setField(st, "id", UUID.randomUUID());
        lenient().when(stocktakes.findById(st.getId())).thenReturn(Optional.of(st));
        lenient().when(stocktakes.findForUpdate(st.getId())).thenReturn(Optional.of(st));
        return st;
    }

    private static StocktakeLine line(StockItem item, String expected, String counted) {
        StocktakeLine l = new StocktakeLine();
        l.setItem(item);
        l.setExpectedQuantity(new BigDecimal(expected));
        l.setCountedQuantity(counted == null ? null : new BigDecimal(counted));
        return l;
    }

    private StockBalance balance(StockItem item, String quantity) {
        StockBalance b = new StockBalance();
        b.setStore(store);
        b.setItem(item);
        b.setQuantityOnHand(new BigDecimal(quantity));
        return b;
    }

    private Store store() {
        Store s = new Store();
        s.setName("Terrace Kitchen");
        ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
        lenient().when(stores.findById(s.getId())).thenReturn(Optional.of(s));
        return s;
    }

    private UnitOfMeasure unit(String code, EUnitGroup group, String factor) {
        UnitOfMeasure u = new UnitOfMeasure();
        u.setCode(code);
        u.setName(code);
        u.setUnitGroup(group);
        u.setFactorToGroupBase(new BigDecimal(factor));
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        lenient().when(units.findById(u.getId())).thenReturn(Optional.of(u));
        return u;
    }

    private StockItem item(String code, UnitOfMeasure baseUnit) {
        StockItem i = new StockItem();
        i.setCode(code);
        i.setName(code);
        i.setBaseUnit(baseUnit);
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        lenient().when(items.findById(i.getId())).thenReturn(Optional.of(i));
        return i;
    }

}
