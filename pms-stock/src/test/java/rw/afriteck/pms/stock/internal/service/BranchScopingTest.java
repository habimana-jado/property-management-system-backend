package rw.afriteck.pms.stock.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.internal.model.Stocktake;
import rw.afriteck.pms.stock.internal.model.Store;
import rw.afriteck.pms.stock.internal.repository.*;

/**
 * Cross-branch ids must look exactly like missing ids: same error, and nothing is written or locked.
 * The guard is mocked so that every store belongs to ANOTHER branch than the caller's.
 */
@ExtendWith(MockitoExtension.class)
class BranchScopingTest {

    @Mock StoreRepository stores;
    @Mock StockItemRepository items;
    @Mock UnitOfMeasureRepository units;
    @Mock StockDocumentRepository documents;
    @Mock DocumentNumberService numbers;
    @Mock StockLedger ledger;
    @Mock StockTakeRepository stocktakes;
    @Mock StockBalanceRepository balances;
    @Mock ItemPackagingRepository packagings;
    @Mock StockOperations operations;
    @Mock BranchGuard guard;

    private StockOperationsImpl operationsService;
    private StocktakeServiceImpl stocktakeService;

    private Store foreignStore;
    private Store foreignDestination;

    @BeforeEach
    void setUp() {
        operationsService = new StockOperationsImpl(stores, items, units, documents, numbers,
                new UnitOfConversionService(packagings), ledger, guard);
        stocktakeService = new StocktakeServiceImpl(stocktakes, stores, items, units, balances, documents,
                new UnitOfConversionService(packagings), operations, guard);

        foreignStore = store(EStoreType.MAIN);
        foreignDestination = store(EStoreType.DEPARTMENT);

        // the caller is in a different branch from every store used here
        lenient().when(guard.inCurrentBranch(any(Store.class))).thenReturn(false);
    }

    @Test
    void postDocument_treatsAStoreFromAnotherBranchAsUnknown() {
        SingleStoreDocumentCommand cmd = new SingleStoreDocumentCommand(
                EDocumentType.RECEIPT, foreignStore.getId(), null, null, null,
                List.of(new DocumentLineInput(UUID.randomUUID(), BigDecimal.ONE, UUID.randomUUID())),
                UUID.randomUUID());

        assertThatThrownBy(() -> operationsService.postDocument(cmd))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown store");
        verifyNoInteractions(ledger, documents);
    }

    @Test
    void transfer_rejectsASourceFromAnotherBranch() {
        TransferCommand cmd = new TransferCommand(foreignStore.getId(), foreignDestination.getId(),
                ETransferPurpose.DISTRIBUTION, null,
                List.of(new DocumentLineInput(UUID.randomUUID(), BigDecimal.ONE, UUID.randomUUID())),
                UUID.randomUUID());

        assertThatThrownBy(() -> operationsService.transfer(cmd))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown store");
        verifyNoInteractions(ledger, documents);
    }

    @Test
    void stocktakeStart_treatsAStoreFromAnotherBranchAsUnknown() {
        assertThatThrownBy(() -> stocktakeService.start(foreignStore.getId(), UUID.randomUUID()))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown store");
        verifyNoInteractions(stocktakes, balances);
    }

    @Test
    void stocktakeActions_treatAStocktakeFromAnotherBranchAsUnknown() {
        Stocktake st = new Stocktake();
        st.setStore(foreignStore);
        st.setStatus(EStocktakeStatus.COUNTING);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(st, "id", id);
        lenient().when(stocktakes.findById(id)).thenReturn(Optional.of(st));
        lenient().when(stocktakes.findForUpdate(id)).thenReturn(Optional.of(st));

        assertThatThrownBy(() -> stocktakeService.submit(id))
                .isInstanceOf(StockRuleViolationException.class).hasMessageContaining("Unknown stocktake");
        assertThatThrownBy(() -> stocktakeService.cancel(id))
                .isInstanceOf(StockRuleViolationException.class).hasMessageContaining("Unknown stocktake");
        assertThatThrownBy(() -> stocktakeService.approve(id, UUID.randomUUID()))
                .isInstanceOf(StockRuleViolationException.class).hasMessageContaining("Unknown stocktake");

        assertThat(st.getStatus()).isEqualTo(EStocktakeStatus.COUNTING);   // untouched
        verifyNoInteractions(operations);
    }

    private Store store(EStoreType type) {
        HotelBranch b = new HotelBranch();
        ReflectionTestUtils.setField(b, "id", UUID.randomUUID());
        Store s = new Store();
        s.setName(type.name() + "-store");
        s.setStoreType(type);
        s.setBranch(b);
        s.setActive(true);
        ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
        lenient().when(stores.findById(s.getId())).thenReturn(Optional.of(s));
        return s;
    }
}