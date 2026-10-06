package rw.afriteck.pms.stock.internal.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.internal.dtos.BalanceKey;
import rw.afriteck.pms.stock.internal.dtos.MovementContext;
import rw.afriteck.pms.stock.internal.enums.EMovementType;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.BranchStockSettingRepository;
import rw.afriteck.pms.stock.internal.repository.StockBalanceRepository;
import rw.afriteck.pms.stock.internal.repository.StockMovementRepository;

@ExtendWith(MockitoExtension.class)
class StockLedgerTest {

    private static final UUID WAITER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MANAGER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CASHIER_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID KEEPER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Mock StockBalanceRepository balances;
    @Mock StockMovementRepository movements;
    @Mock BalanceInitializer initializer;
    @Mock BranchStockSettingRepository settings;

    private StockLedger ledger;

    private final UUID branchId = UUID.randomUUID();
    private Store store;
    private StockItem item;

    @BeforeEach
    void setUp() {
        ledger = new StockLedger(balances, movements, initializer, settings);

        HotelBranch branch = new HotelBranch();
        ReflectionTestUtils.setField(branch, "id", branchId);
        store = store(UUID.randomUUID(), branch);
        item = item(UUID.randomUUID(), "SODA");

        // save() returns the entity it was given, like Spring Data does
        lenient().when(movements.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ============================ lockAll ============================

    @Test
    void lockAll_usesOneLockingQueryPerStore_notOnePerItem() {
        UUID s = new UUID(1, 1);
        List<UUID> itemIds = List.of(new UUID(2, 1), new UUID(2, 2), new UUID(2, 3));
        stubFindAllForUpdate(s, itemIds);

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(
                itemIds.stream().map(i -> new BalanceKey(s, i)).toList());

        verify(balances, times(1)).findAllForUpdate(eq(s), anyCollection());
        assertThat(locked).hasSize(3);
        verifyNoInteractions(initializer);                      // everything existed: nothing to create
    }

    @Test
    void lockAll_locksStoresInIdOrder_andItemsWithinAStoreInIdOrder_whateverTheInputOrder() {
        UUID s1 = new UUID(1, 1), s2 = new UUID(1, 2);
        UUID i1 = new UUID(2, 1), i2 = new UUID(2, 2);
        stubFindAllForUpdate(s1, List.of(i1, i2));
        stubFindAllForUpdate(s2, List.of(i1, i2));

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(List.of(
                new BalanceKey(s2, i2), new BalanceKey(s1, i2), new BalanceKey(s2, i1), new BalanceKey(s1, i1)));

        InOrder order = inOrder(balances);
        ArgumentCaptor<Collection<UUID>> firstItems = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<UUID>> secondItems = ArgumentCaptor.forClass(Collection.class);
        order.verify(balances).findAllForUpdate(eq(s1), firstItems.capture());     // store 1 before store 2
        order.verify(balances).findAllForUpdate(eq(s2), secondItems.capture());
        assertThat(firstItems.getValue()).containsExactly(i1, i2);                 // sorted within the store
        assertThat(secondItems.getValue()).containsExactly(i1, i2);
        assertThat(locked.keySet()).containsExactly(
                new BalanceKey(s1, i1), new BalanceKey(s1, i2), new BalanceKey(s2, i1), new BalanceKey(s2, i2));
    }

    @Test
    void lockAll_collapsesDuplicateKeys() {
        UUID s = new UUID(1, 1), i = new UUID(2, 1);
        stubFindAllForUpdate(s, List.of(i));
        BalanceKey key = new BalanceKey(s, i);

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(List.of(key, key, key));

        assertThat(locked).hasSize(1);
        ArgumentCaptor<Collection<UUID>> requested = ArgumentCaptor.forClass(Collection.class);
        verify(balances, times(1)).findAllForUpdate(eq(s), requested.capture());
        assertThat(requested.getValue()).containsExactly(i);
    }

    @Test
    void lockAll_createsOnlyTheMissingBalances_thenLocksAgainIncludingTheNewRows() {
        UUID s = new UUID(1, 1);
        UUID existing = new UUID(2, 1), missing = new UUID(2, 2);
        when(balances.findAllForUpdate(eq(s), anyCollection()))
                .thenReturn(List.of(balanceFor(s, existing)))                       // first query: one row missing
                .thenReturn(List.of(balanceFor(s, existing), balanceFor(s, missing)));   // after creation: all there

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(
                List.of(new BalanceKey(s, existing), new BalanceKey(s, missing)));

        verify(initializer).createIfAbsent(s, missing);
        verify(initializer, never()).createIfAbsent(s, existing);
        verify(balances, times(2)).findAllForUpdate(eq(s), anyCollection());
        assertThat(locked).containsOnlyKeys(new BalanceKey(s, existing), new BalanceKey(s, missing));
    }

    @Test
    void lockAll_ignoresAConcurrentCreate_andStillLocks() {
        UUID s = new UUID(1, 1), i = new UUID(2, 1);
        when(balances.findAllForUpdate(eq(s), anyCollection()))
                .thenReturn(List.of())                                              // not there yet
                .thenReturn(List.of(balanceFor(s, i)));                             // created by the other transaction
        doThrow(new DataIntegrityViolationException("created by another transaction"))
                .when(initializer).createIfAbsent(s, i);

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(List.of(new BalanceKey(s, i)));

        assertThat(locked).containsKey(new BalanceKey(s, i));
    }

    @Test
    void lockAll_failsLoudly_ifABalanceIsStillMissingAfterCreation() {
        UUID s = new UUID(1, 1), i = new UUID(2, 1);
        when(balances.findAllForUpdate(eq(s), anyCollection())).thenReturn(List.of());   // never appears

        assertThatThrownBy(() -> ledger.lockAll(List.of(new BalanceKey(s, i))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Balance rows missing");
    }

    @Test
    void lockAll_withNoKeys_doesNothing() {
        Map<BalanceKey, StockBalance> locked = ledger.lockAll(List.of());

        assertThat(locked).isEmpty();
        verifyNoInteractions(balances, initializer);
    }

    // ============================ post: bookkeeping ============================

    @Test
    void post_sale_reducesTheBalance_andRecordsTheMovement() {
        StockBalance balance = balance("10");
        allowNegativeStock(false);

        StockMovement m = ledger.post(balance, EMovementType.SALE, new BigDecimal("-3"),
                MovementContext.forSale(EMovementSourceType.TABLE_BILL_LINE,
                        UUID.fromString("00000000-0000-0000-0000-00000000000a"),
                        UUID.fromString("00000000-0000-0000-0000-00000000000b"), WAITER_ID));

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("7");
        assertThat(m.getQuantity()).isEqualByComparingTo("-3");
        assertThat(m.getBalanceAfter()).isEqualByComparingTo("7");
        assertThat(m.getMovementType()).isEqualTo(EMovementType.SALE);
        assertThat(m.getStore()).isSameAs(store);
        assertThat(m.getItem()).isSameAs(item);
        assertThat(m.getPerformedBy()).isEqualTo(WAITER_ID);
        assertThat(m.getOccurredAt()).isNotNull();
        assertThat(m.getSourceType()).isEqualTo(EMovementSourceType.TABLE_BILL_LINE);
        assertThat(m.getSourceId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
        assertThat(m.getSourceLineId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-00000000000b"));
        assertThat(m.getReversalOf()).isNull();
        verify(movements).save(m);
    }

    @Test
    void post_receipt_increasesTheBalance_andLinksTheDocumentLine() {
        StockBalance balance = balance("2");
        StockDocumentLine line = new StockDocumentLine();

        StockMovement m = ledger.post(balance, EMovementType.RECEIPT, new BigDecimal("120"),
                MovementContext.forDocument(line, "Delivery note 55", KEEPER_ID));

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("122");
        assertThat(m.getBalanceAfter()).isEqualByComparingTo("122");
        assertThat(m.getDocumentLine()).isSameAs(line);
        assertThat(m.getNote()).isEqualTo("Delivery note 55");
        assertThat(m.getPerformedBy()).isEqualTo(KEEPER_ID);
        assertThat(m.getSourceType()).isNull();
    }

    @Test
    void post_canDeplete_exactlyToZero() {
        StockBalance balance = balance("5");
        allowNegativeStock(false);

        StockMovement m = ledger.post(balance, EMovementType.SALE, new BigDecimal("-5"), saleContext());

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("0");
        assertThat(m.getBalanceAfter()).isEqualByComparingTo("0");
    }

    // ============================ post: the negative-stock policy ============================

    @ParameterizedTest
    @EnumSource(value = EMovementType.class,
            names = {"TRANSFER_OUT", "SUPPLIER_RETURN", "DAMAGE_WRITE_OFF"})
    void post_outflowsThatMayNeverGoNegative_areRefused_andNeverConsultTheSetting(EMovementType type) {
        StockBalance balance = balance("2");

        assertThatThrownBy(() -> ledger.post(balance, type, new BigDecimal("-5"), saleContext()))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("2");   // untouched
        verify(movements, never()).save(any());
        verifyNoInteractions(settings);
    }

    @Test
    void post_insufficientStock_reportsStoreItemAvailableAndRequired() {
        StockBalance balance = balance("2");

        assertThatThrownBy(() -> ledger.post(balance, EMovementType.TRANSFER_OUT,
                new BigDecimal("-5"), saleContext()))
                .isInstanceOfSatisfying(InsufficientStockException.class, e -> {
                    assertThat(e.getAvailable()).isEqualByComparingTo("2");
                    assertThat(e.getRequired()).isEqualByComparingTo("5");
                    assertThat(e.getMessage()).contains("SODA").contains("Terrace Kitchen");
                });
    }

    @ParameterizedTest
    @EnumSource(value = EMovementType.class, names = {"SALE", "CONSUMPTION"})
    void post_saleAndConsumption_areRefused_whenTheBranchSettingForbidsNegativeStock(EMovementType type) {
        StockBalance balance = balance("2");
        allowNegativeStock(false);

        assertThatThrownBy(() -> ledger.post(balance, type, new BigDecimal("-5"), saleContext()))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("2");
        verify(movements, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = EMovementType.class, names = {"SALE", "CONSUMPTION"})
    void post_saleAndConsumption_mayGoNegative_whenTheBranchSettingAllowsIt(EMovementType type) {
        StockBalance balance = balance("2");
        allowNegativeStock(true);

        StockMovement m = ledger.post(balance, type, new BigDecimal("-5"), saleContext());

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("-3");
        assertThat(m.getBalanceAfter()).isEqualByComparingTo("-3");
    }

    @Test
    void post_withoutABranchSettingsRow_negativeStockIsNotAllowed() {
        StockBalance balance = balance("2");
        when(settings.findByBranchId(branchId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ledger.post(balance, EMovementType.SALE, new BigDecimal("-5"), saleContext()))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void post_consultsTheSettingOfTheStoresOwnBranch() {
        StockBalance balance = balance("10");
        allowNegativeStock(false);

        ledger.post(balance, EMovementType.SALE, new BigDecimal("-1"), saleContext());

        verify(settings, never()).findByBranchId(any());       // not negative: no need to look it up
        // now force the check to be needed and verify the right branch is queried
        StockBalance low = balance("0");
        assertThatThrownBy(() -> ledger.post(low, EMovementType.SALE, new BigDecimal("-1"), saleContext()))
                .isInstanceOf(InsufficientStockException.class);
        verify(settings).findByBranchId(branchId);
    }

    @Test
    void post_adjustment_alwaysPosts_evenBelowZero_withoutConsultingTheSetting() {
        StockBalance balance = balance("1");

        StockMovement m = ledger.post(balance, EMovementType.ADJUSTMENT, new BigDecimal("-3"),
                MovementContext.forDocument(new StockDocumentLine(), "Stocktake", MANAGER_ID));

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("-2");
        assertThat(m.getBalanceAfter()).isEqualByComparingTo("-2");
        verifyNoInteractions(settings);
    }

    // ============================ reverse ============================

    @Test
    void reverse_restoresStock_andLinksBackToTheOriginal() {
        StockBalance balance = balance("7");                    // after the original sale of 3
        UUID sourceId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        StockMovement original = original(EMovementType.SALE, "-3", sourceId, lineId);

        StockMovement reversal = ledger.reverse(balance, original, "Line voided", MANAGER_ID);

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("10");
        assertThat(reversal.getMovementType()).isEqualTo(EMovementType.SALE);   // same type, opposite sign
        assertThat(reversal.getQuantity()).isEqualByComparingTo("3");
        assertThat(reversal.getBalanceAfter()).isEqualByComparingTo("10");
        assertThat(reversal.getReversalOf()).isSameAs(original);
        assertThat(reversal.getReason()).isEqualTo("Line voided");
        assertThat(reversal.getPerformedBy()).isEqualTo(MANAGER_ID);
        assertThat(reversal.getSourceType()).isEqualTo(EMovementSourceType.TABLE_BILL_LINE);
        assertThat(reversal.getSourceId()).isEqualTo(sourceId);
        assertThat(reversal.getSourceLineId()).isEqualTo(lineId);
    }

    @Test
    void reverse_keepsTheOriginalsDocumentLine() {
        StockDocumentLine line = new StockDocumentLine();
        StockMovement original = original(EMovementType.TRANSFER_OUT, "-4", null, null);
        original.setDocumentLine(line);

        StockMovement reversal = ledger.reverse(balance("0"), original, "Mistake", MANAGER_ID);

        assertThat(reversal.getDocumentLine()).isSameAs(line);
    }

    @Test
    void reverse_ofAnInflowThatWasAlreadyConsumed_isRefused() {
        StockBalance balance = balance("2");                    // 5 were received, 3 already sold
        StockMovement receipt = original(EMovementType.RECEIPT, "5", null, null);

        assertThatThrownBy(() -> ledger.reverse(balance, receipt, "Wrong delivery", MANAGER_ID))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(balance.getQuantityOnHand()).isEqualByComparingTo("2");
    }

    // ============================ fixtures ============================

    /** Makes findAllForUpdate(storeId, ...) answer with a balance for each requested item that is in 'existing'. */
    private void stubFindAllForUpdate(UUID storeId, List<UUID> existing) {
        lenient().when(balances.findAllForUpdate(eq(storeId), anyCollection())).thenAnswer(inv -> {
            Collection<UUID> requested = inv.getArgument(1);
            List<StockBalance> found = new ArrayList<>();
            for (UUID itemId : requested) {
                if (existing.contains(itemId)) found.add(balanceFor(storeId, itemId));
            }
            return found;
        });
    }

    private StockBalance balanceFor(UUID storeId, UUID itemId) {
        StockBalance b = new StockBalance();
        b.setStore(store(storeId, null));
        b.setItem(item(itemId, "ITEM"));
        return b;
    }

    private void allowNegativeStock(boolean allowed) {
        BranchStockSetting s = new BranchStockSetting();
        s.setAllowNegativeStock(allowed);
        lenient().when(settings.findByBranchId(branchId)).thenReturn(Optional.of(s));
    }

    private StockBalance balance(String quantity) {
        StockBalance b = new StockBalance();
        b.setStore(store);
        b.setItem(item);
        b.setQuantityOnHand(new BigDecimal(quantity));
        return b;
    }

    private MovementContext saleContext() {
        return MovementContext.forSale(EMovementSourceType.POS_SALE_LINE, UUID.randomUUID(), UUID.randomUUID(), CASHIER_ID);
    }

    private StockMovement original(EMovementType type, String quantity, UUID sourceId, UUID lineId) {
        StockMovement m = new StockMovement();
        m.setStore(store);
        m.setItem(item);
        m.setMovementType(type);
        m.setQuantity(new BigDecimal(quantity));
        if (sourceId != null) {
            m.setSourceType(EMovementSourceType.TABLE_BILL_LINE);
            m.setSourceId(sourceId);
            m.setSourceLineId(lineId);
        }
        ReflectionTestUtils.setField(m, "id", UUID.randomUUID());
        return m;
    }

    private static Store store(UUID id, HotelBranch branch) {
        Store s = new Store();
        s.setName("Terrace Kitchen");
        s.setBranch(branch);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    private static StockItem item(UUID id, String name) {
        StockItem i = new StockItem();
        i.setCode(name);
        i.setName(name);
        ReflectionTestUtils.setField(i, "id", id);
        return i;
    }
}
