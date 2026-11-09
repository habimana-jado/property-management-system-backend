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
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentStatus;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.internal.dtos.BalanceKey;
import rw.afriteck.pms.stock.internal.dtos.MovementContext;
import rw.afriteck.pms.stock.internal.enums.*;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.*;

@ExtendWith(MockitoExtension.class)
class StockOperationsImplTest {
    @Mock
    StoreRepository stores;
    @Mock
    StockItemRepository items;
    @Mock
    UnitOfMeasureRepository units;
    @Mock
    StockDocumentRepository documents;
    @Mock
    DocumentNumberService numbers;
    @Mock
    StockLedger ledger;
    @Mock
    ItemPackagingRepository packagings;

    private StockOperationsImpl service;

    private final Map<UUID, Store> knownStores = new HashMap<>();
    private final Map<UUID, StockItem> knownItems = new HashMap<>();

    private HotelBranch branch;
    private UnitOfMeasure pcs, g, kg, carton;
    private StockItem soda;

    @BeforeEach
    void setUp() {
        service = new StockOperationsImpl(stores, items, units, documents, numbers,
                new UnitOfConversionService(packagings), ledger);

        branch = branch();
        pcs    = unit("PCS",    EUnitGroup.COUNT,  "1");
        g      = unit("G",      EUnitGroup.WEIGHT, "1");
        kg     = unit("KG",     EUnitGroup.WEIGHT, "1000");
        carton = unit("CARTON", EUnitGroup.COUNT,  "1");
        soda   = item("SODA", pcs);

        lenient().when(numbers.next(any(), any())).thenReturn("KGL-DOC-000001");

        // save assigns an id, like JPA would on persist
        lenient().when(documents.save(any(StockDocument.class))).thenAnswer(inv -> {
            StockDocument d = inv.getArgument(0);
            ReflectionTestUtils.setField(d, "id", UUID.randomUUID());
            return d;
        });

        // lockAll hands back one balance per distinct key
        lenient().when(ledger.lockAll(anyCollection())).thenAnswer(inv -> {
            Collection<BalanceKey> keys = inv.getArgument(0);
            Map<BalanceKey, StockBalance> map = new LinkedHashMap<>();
            for (BalanceKey k : keys) {
                map.computeIfAbsent(k, key -> {
                    StockBalance b = new StockBalance();
                    b.setStore(knownStores.get(key.storeId()));
                    b.setItem(knownItems.get(key.itemId()));
                    return b;
                });
            }
            return map;
        });
    }

    // =====================================================================
    // postDocument: the document itself
    // =====================================================================

    @Test
    void receipt_createsAPostedDocumentWithHeaderAndLines() {
        Store main = store(EStoreType.MAIN);

        UUID id = service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(soda, "10", pcs)));

        StockDocument doc = savedDocument();
        assertThat(id).isEqualTo(doc.getId());
        assertThat(doc.getDocumentType()).isEqualTo(EDocumentType.RECEIPT);
        assertThat(doc.getDocumentNo()).isEqualTo("KGL-DOC-000001");
        assertThat(doc.getStore()).isSameAs(main);
        assertThat(doc.getCounterpartStore()).isNull();
        assertThat(doc.getPurpose()).isNull();
        assertThat(doc.getStatus()).isEqualTo(EDocumentStatus.POSTED);
        assertThat(doc.getPostedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        assertThat(doc.getPostedAt()).isNotNull();
        assertThat(doc.getSupplierName()).isEqualTo("Acme Ltd");
        assertThat(doc.getExternalReference()).isEqualTo("DN-55");
        assertThat(doc.getNote()).isEqualTo("note text");

        assertThat(doc.getLines()).hasSize(1);
        StockDocumentLine line = doc.getLines().get(0);
        assertThat(line.getItem()).isSameAs(soda);
        assertThat(line.getQuantity()).isEqualByComparingTo("10");
        assertThat(line.getEnteredQuantity()).isEqualByComparingTo("10");
        assertThat(line.getEnteredUnit()).isSameAs(pcs);
        assertThat(line.getDocument()).isSameAs(doc);
    }

    @Test
    void documentNumber_comesFromTheStoresBranchAndTheEDocumentType() {
        Store main = store(EStoreType.MAIN);

        service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(soda, "1", pcs)));

        verify(numbers).next(branch, EDocumentType.RECEIPT);
    }

    @Test
    void lineQuantities_areConvertedToTheItemsBaseUnit_andTheEnteredValuesKept() {   // example A
        Store main = store(EStoreType.MAIN);
        when(packagings.findByItemIdAndUnitId(soda.getId(), carton.getId()))
                .thenReturn(Optional.of(packaging(soda, carton, "24")));

        service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(soda, "5", carton)));

        StockDocumentLine line = savedDocument().getLines().get(0);
        assertThat(line.getQuantity()).isEqualByComparingTo("120");
        assertThat(line.getEnteredQuantity()).isEqualByComparingTo("5");
        assertThat(line.getEnteredUnit()).isSameAs(carton);
        assertThat(verifyPosted(1).get(0).delta()).isEqualByComparingTo("120");
    }

    // =====================================================================
    // postDocument: movement type and direction per document type
    // =====================================================================

    @ParameterizedTest(name = "{0} on a {1} store -> {2} movement, delta {3}")
    @CsvSource({
            "RECEIPT,          MAIN,       RECEIPT,          10",
            "OPENING_BALANCE,  MAIN,       OPENING_BALANCE,  10",
            "CUSTOMER_RETURN,  DEPARTMENT, CUSTOMER_RETURN,  10",
            "SUPPLIER_RETURN,  MAIN,       SUPPLIER_RETURN,  -10",
            "WRITE_OFF,        DAMAGED,    DAMAGE_WRITE_OFF, -10",
            "CONSUMPTION,      DEPARTMENT, CONSUMPTION,      -10"
    })
    void eachEDocumentType_postsTheRightMovementType_inTheRightDirection(
            EDocumentType docType, EStoreType EStoreType, EMovementType expectedType, String expectedDelta) {
        Store s = store(EStoreType);

        service.postDocument(docCommand(docType, s, in(soda, "10", pcs)));

        Posted p = verifyPosted(1).get(0);
        assertThat(p.type()).isEqualTo(expectedType);
        assertThat(p.delta()).isEqualByComparingTo(expectedDelta);
        assertThat(p.storeId()).isEqualTo(s.getId());
        assertThat(p.itemCode()).isEqualTo("SODA");
    }

    @ParameterizedTest(name = "adjustment entered {0} -> stored and posted {1}")
    @CsvSource({"4, 4", "-3, -3"})
    void adjustment_isSigned_inTheDocumentLineAndInTheMovement(String entered, String expected) {
        Store dept = store(EStoreType.DEPARTMENT);

        service.postDocument(docCommand(EDocumentType.ADJUSTMENT, dept, in(soda, entered, pcs)));

        assertThat(savedDocument().getLines().get(0).getQuantity()).isEqualByComparingTo(expected);
        Posted p = verifyPosted(1).get(0);
        assertThat(p.type()).isEqualTo(EMovementType.ADJUSTMENT);
        assertThat(p.delta()).isEqualByComparingTo(expected);
    }

    @ParameterizedTest(name = "{0} is allowed on a {1} store")
    @CsvSource({
            "ADJUSTMENT,      MAIN",       "ADJUSTMENT,      DEPARTMENT", "ADJUSTMENT,      DAMAGED",
            "OPENING_BALANCE, MAIN",       "OPENING_BALANCE, DEPARTMENT", "OPENING_BALANCE, DAMAGED"
    })
    void adjustmentAndOpeningBalance_areAllowedOnAnyEStoreType(EDocumentType docType, EStoreType EStoreType) {
        Store s = store(EStoreType);

        service.postDocument(docCommand(docType, s, in(soda, "5", pcs)));

        verifyPosted(1);
    }

    // =====================================================================
    // postDocument: locking and posting
    // =====================================================================

    @Test
    void document_isSaved_thenBalancesAreLocked_thenMovementsPosted() {
        Store main = store(EStoreType.MAIN);
        StockItem water = item("WATER", pcs);

        service.postDocument(docCommand(EDocumentType.RECEIPT, main,
                in(soda, "10", pcs), in(water, "20", pcs)));

        InOrder order = inOrder(documents, ledger);
        order.verify(documents).save(any(StockDocument.class));
        order.verify(ledger).lockAll(anyCollection());
        order.verify(ledger, times(2)).post(any(), any(), any(), any());
    }

    @Test
    void locksTheStoreAndItemOfEveryLine() {
        Store main = store(EStoreType.MAIN);
        StockItem water = item("WATER", pcs);

        service.postDocument(docCommand(EDocumentType.RECEIPT, main,
                in(soda, "10", pcs), in(water, "20", pcs)));

        assertThat(lockedKeys()).containsExactlyInAnyOrder(
                new BalanceKey(main.getId(), soda.getId()),
                new BalanceKey(main.getId(), water.getId()));
    }

    @Test
    void everyMovement_linksToItsOwnDocumentLine_andCarriesTheNoteAndPerformer() {
        Store main = store(EStoreType.MAIN);
        StockItem water = item("WATER", pcs);

        service.postDocument(docCommand(EDocumentType.RECEIPT, main,
                in(soda, "10", pcs), in(water, "20", pcs)));

        List<StockDocumentLine> lines = savedDocument().getLines();
        List<Posted> posted = verifyPosted(2);
        assertThat(posted.get(0).ctx().documentLine()).isSameAs(lines.get(0));
        assertThat(posted.get(1).ctx().documentLine()).isSameAs(lines.get(1));
        assertThat(posted).allSatisfy(p -> {
            assertThat(p.ctx().note()).isEqualTo("note text");
            assertThat(p.ctx().performedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        });
    }

    @Test
    void insufficientStockFromTheLedger_propagates() {
        Store main = store(EStoreType.MAIN);
        when(ledger.post(any(), any(), any(), any())).thenThrow(
                new InsufficientStockException("Main", "SODA", BigDecimal.ONE, BigDecimal.TEN));

        assertThatThrownBy(() -> service.postDocument(
                docCommand(EDocumentType.SUPPLIER_RETURN, main, in(soda, "10", pcs))))
                .isInstanceOf(InsufficientStockException.class);
    }

    // =====================================================================
    // postDocument: rejections
    // =====================================================================

    @ParameterizedTest(name = "{0} on a {1} store is rejected")
    @CsvSource({
            "RECEIPT,         DEPARTMENT, Main store only",
            "RECEIPT,         DAMAGED,    Main store only",
            "SUPPLIER_RETURN, DEPARTMENT, Main store only",
            "SUPPLIER_RETURN, DAMAGED,    Main store only",
            "WRITE_OFF,       MAIN,       damaged store",
            "WRITE_OFF,       DEPARTMENT, damaged store",
            "CONSUMPTION,     DAMAGED,    Not allowed on the damaged store",
            "CUSTOMER_RETURN, DAMAGED,    Not allowed on the damaged store"
    })
    void EDocumentTypeOnTheWrongEStoreType_isRejected_beforeAnythingIsNumberedOrSaved(
            EDocumentType docType, EStoreType EStoreType, String message) {
        Store s = store(EStoreType);

        assertThatThrownBy(() -> service.postDocument(docCommand(docType, s, in(soda, "5", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining(message);

        verifyNothingNumberedSavedOrPosted();
    }

    @Test
    void transferType_isRejected_becauseTransfersHaveTheirOwnMethod() {
        Store main = store(EStoreType.MAIN);

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.TRANSFER, main, in(soda, "5", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("transfer()");

        verifyNothingNumberedSavedOrPosted();
    }

    @Test
    void unknownStore_isRejected() {
        SingleStoreDocumentCommand cmd = new SingleStoreDocumentCommand(EDocumentType.RECEIPT, UUID.randomUUID(),
                null, null, null, List.of(in(soda, "5", pcs)), UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThatThrownBy(() -> service.postDocument(cmd))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown store");
    }

    @Test
    void inactiveStore_isRejected() {
        Store main = store(EStoreType.MAIN, branch, false);

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(soda, "5", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("inactive");

        verifyNothingNumberedSavedOrPosted();
    }

    @Test
    void emptyLines_areRejected() {
        Store main = store(EStoreType.MAIN);

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main)))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("at least one line");

        verifyNothingSavedOrPosted();
    }

    @Test
    void nonPositiveQuantity_isRejectedForEveryTypeExceptAdjustment() {
        Store main = store(EStoreType.MAIN);

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(soda, "-5", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("must be positive");

        verifyNothingSavedOrPosted();
    }

    @ParameterizedTest
    @CsvSource({"RECEIPT", "ADJUSTMENT"})
    void zeroQuantity_isRejected_evenForAdjustments(EDocumentType docType) {
        Store main = store(EStoreType.MAIN);

        assertThatThrownBy(() -> service.postDocument(docCommand(docType, main, in(soda, "0", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("rounds to zero");

        verifyNothingSavedOrPosted();
    }

    @Test
    void quantityThatRoundsToZeroInTheBaseUnit_isRejected() {
        Store main = store(EStoreType.MAIN);
        StockItem salt = item("SALT", kg);                       // counted in kg, entered in grams

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(salt, "0.0004", g))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("rounds to zero");
    }

    @Test
    void unknownItem_inactiveItem_andUnknownUnit_areRejected() {
        Store main = store(EStoreType.MAIN);
        StockItem dead = item("DEAD", pcs);
        dead.setActive(false);

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main,
                new DocumentLineInput(UUID.randomUUID(), BigDecimal.ONE, pcs.getId()))))
                .hasMessageContaining("Unknown stock item");

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main, in(dead, "1", pcs))))
                .hasMessageContaining("inactive");

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main,
                new DocumentLineInput(soda.getId(), BigDecimal.ONE, UUID.randomUUID()))))
                .hasMessageContaining("Unknown unit");

        verifyNothingSavedOrPosted();
    }

    @Test
    void oneBadLine_rejectsTheWholeDocument_noPartialWork() {
        Store main = store(EStoreType.MAIN);
        StockItem water = item("WATER", pcs);

        assertThatThrownBy(() -> service.postDocument(docCommand(EDocumentType.RECEIPT, main,
                in(soda, "10", pcs), in(water, "-1", pcs))))
                .isInstanceOf(StockRuleViolationException.class);

        verifyNothingSavedOrPosted();
    }

    // =====================================================================
    // transfer: the document and the two legs
    // =====================================================================

    @Test
    void distribution_createsATransferDocument_withBothStores() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);

        UUID id = service.transfer(transfer(ETransferPurpose.DISTRIBUTION, main, terrace, in(soda, "10", pcs)));

        StockDocument doc = savedDocument();
        assertThat(id).isEqualTo(doc.getId());
        assertThat(doc.getDocumentType()).isEqualTo(EDocumentType.TRANSFER);
        assertThat(doc.getPurpose()).isEqualTo(ETransferPurpose.DISTRIBUTION);
        assertThat(doc.getStore()).isSameAs(main);
        assertThat(doc.getCounterpartStore()).isSameAs(terrace);
        assertThat(doc.getStatus()).isEqualTo(EDocumentStatus.POSTED);
        assertThat(doc.getPostedBy()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        assertThat(doc.getNote()).isEqualTo("transfer note");
        verify(numbers).next(branch, rw.afriteck.pms.stock.api.enums.EDocumentType.TRANSFER);
    }

    @Test
    void transfer_postsOutThenIn_forEachLine_onTheRightStores() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);
        StockItem water = item("WATER", pcs);

        service.transfer(transfer(ETransferPurpose.DISTRIBUTION, main, terrace,
                in(soda, "10", pcs), in(water, "6", pcs)));

        List<Posted> posted = verifyPosted(4);
        assertPosted(posted.get(0), main,    "SODA",  EMovementType.TRANSFER_OUT, "-10");
        assertPosted(posted.get(1), terrace, "SODA",  EMovementType.TRANSFER_IN,  "10");
        assertPosted(posted.get(2), main,    "WATER", EMovementType.TRANSFER_OUT, "-6");
        assertPosted(posted.get(3), terrace, "WATER", EMovementType.TRANSFER_IN,  "6");
    }

    @Test
    void transfer_bothLegsOfALineShareTheSameDocumentLine() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);

        service.transfer(transfer(ETransferPurpose.DISTRIBUTION, main, terrace, in(soda, "10", pcs)));

        StockDocumentLine line = savedDocument().getLines().get(0);
        List<Posted> posted = verifyPosted(2);
        assertThat(posted.get(0).ctx().documentLine()).isSameAs(line);
        assertThat(posted.get(1).ctx().documentLine()).isSameAs(line);
    }

    @Test
    void transfer_locksBothStoresBalances_forEveryLine_beforePosting() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);
        StockItem water = item("WATER", pcs);

        service.transfer(transfer(ETransferPurpose.DISTRIBUTION, main, terrace,
                in(soda, "10", pcs), in(water, "6", pcs)));

        assertThat(lockedKeys()).containsExactlyInAnyOrder(
                new BalanceKey(main.getId(), soda.getId()),    new BalanceKey(terrace.getId(), soda.getId()),
                new BalanceKey(main.getId(), water.getId()),   new BalanceKey(terrace.getId(), water.getId()));
        InOrder order = inOrder(ledger);
        order.verify(ledger).lockAll(anyCollection());
        order.verify(ledger, times(4)).post(any(), any(), any(), any());
    }

    @Test
    void transfer_convertsEnteredUnitsToTheBaseUnit_forBothLegs() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);
        when(packagings.findByItemIdAndUnitId(soda.getId(), carton.getId()))
                .thenReturn(Optional.of(packaging(soda, carton, "24")));

        service.transfer(transfer(ETransferPurpose.DISTRIBUTION, main, terrace, in(soda, "2", carton)));

        List<Posted> posted = verifyPosted(2);
        assertThat(posted.get(0).delta()).isEqualByComparingTo("-48");
        assertThat(posted.get(1).delta()).isEqualByComparingTo("48");
    }

    @Test
    void whenTheOutLegFails_theInLegIsNeverPosted() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);
        when(ledger.post(any(), eq(EMovementType.TRANSFER_OUT), any(), any())).thenThrow(
                new InsufficientStockException("Main", "SODA", BigDecimal.ONE, BigDecimal.TEN));

        assertThatThrownBy(() -> service.transfer(
                transfer(ETransferPurpose.DISTRIBUTION, main, terrace, in(soda, "10", pcs))))
                .isInstanceOf(InsufficientStockException.class);

        verify(ledger, never()).post(any(), eq(EMovementType.TRANSFER_IN), any(), any());
    }

    // =====================================================================
    // transfer: which store types each purpose allows
    // =====================================================================

    @ParameterizedTest(name = "{0}: {1} -> {2} is allowed")
    @CsvSource({
            "DISTRIBUTION,  MAIN,       DEPARTMENT",
            "RETURN,        DEPARTMENT, MAIN",
            "DAMAGE_REPORT, MAIN,       DAMAGED",
            "DAMAGE_REPORT, DEPARTMENT, DAMAGED"
    })
    void validPurposeAndEStoreTypeCombinations_areAccepted(
            ETransferPurpose purpose, EStoreType fromType, EStoreType toType) {
        Store from = store(fromType);
        Store to = store(toType);

        service.transfer(transfer(purpose, from, to, in(soda, "3", pcs)));

        verifyPosted(2);
    }

    @ParameterizedTest(name = "{0}: {1} -> {2} is rejected")
    @CsvSource({
            "DISTRIBUTION,  DEPARTMENT, DEPARTMENT, Distribution goes from the main store",
            "DISTRIBUTION,  DEPARTMENT, MAIN,       Distribution goes from the main store",
            "DISTRIBUTION,  MAIN,       DAMAGED,    Distribution goes from the main store",
            "DISTRIBUTION,  DAMAGED,    DEPARTMENT, Distribution goes from the main store",
            "RETURN,        MAIN,       DEPARTMENT, Return goes from a department store",
            "RETURN,        DEPARTMENT, DEPARTMENT, Return goes from a department store",
            "RETURN,        DAMAGED,    MAIN,       Return goes from a department store",
            "DAMAGE_REPORT, DEPARTMENT, DEPARTMENT, Damage reports go into the damaged store",
            "DAMAGE_REPORT, DAMAGED,    DAMAGED,    Damage reports go into the damaged store",
            "DAMAGE_REPORT, DAMAGED,    MAIN,       Damage reports go into the damaged store"
    })
    void invalidPurposeAndEStoreTypeCombinations_areRejected_beforeAnythingIsNumberedOrSaved(
            ETransferPurpose purpose, EStoreType fromType, EStoreType toType, String message) {
        Store from = store(fromType);
        Store to = store(toType);

        assertThatThrownBy(() -> service.transfer(transfer(purpose, from, to, in(soda, "3", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining(message);

        verifyNothingNumberedSavedOrPosted();
    }

    // =====================================================================
    // transfer: other rejections
    // =====================================================================

    @Test
    void transferToTheSameStore_isRejected() {
        Store main = store(EStoreType.MAIN);

        assertThatThrownBy(() -> service.transfer(
                transfer(ETransferPurpose.DISTRIBUTION, main, main, in(soda, "3", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("must differ");

        verifyNothingNumberedSavedOrPosted();
    }

    @Test
    void transferBetweenBranches_isRejected() {
        Store main = store(EStoreType.MAIN);
        Store otherBranchTerrace = store(EStoreType.DEPARTMENT, branch(), true);

        assertThatThrownBy(() -> service.transfer(
                transfer(ETransferPurpose.DISTRIBUTION, main, otherBranchTerrace, in(soda, "3", pcs))))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("same branch");

        verifyNothingNumberedSavedOrPosted();
    }

    @Test
    void inactiveSourceOrDestination_isRejected() {
        Store main = store(EStoreType.MAIN);
        Store inactiveTerrace = store(EStoreType.DEPARTMENT, branch, false);
        Store inactiveMain = store(EStoreType.MAIN, branch, false);
        Store terrace = store(EStoreType.DEPARTMENT);

        assertThatThrownBy(() -> service.transfer(
                transfer(ETransferPurpose.DISTRIBUTION, main, inactiveTerrace, in(soda, "3", pcs))))
                .hasMessageContaining("inactive");
        assertThatThrownBy(() -> service.transfer(
                transfer(ETransferPurpose.DISTRIBUTION, inactiveMain, terrace, in(soda, "3", pcs))))
                .hasMessageContaining("inactive");

        verifyNothingNumberedSavedOrPosted();
    }

    @Test
    void unknownDestinationStore_isRejected() {
        Store main = store(EStoreType.MAIN);
        TransferCommand cmd = new TransferCommand(main.getId(), UUID.randomUUID(),
                ETransferPurpose.DISTRIBUTION, null, List.of(in(soda, "3", pcs)), UUID.fromString("00000000-0000-0000-0000-000000000002"));

        assertThatThrownBy(() -> service.transfer(cmd))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("Unknown store");
    }

    @Test
    void transferWithoutLines_orWithNonPositiveQuantity_isRejected() {
        Store main = store(EStoreType.MAIN);
        Store terrace = store(EStoreType.DEPARTMENT);

        assertThatThrownBy(() -> service.transfer(transfer(ETransferPurpose.DISTRIBUTION, main, terrace)))
                .hasMessageContaining("at least one line");
        assertThatThrownBy(() -> service.transfer(
                transfer(ETransferPurpose.DISTRIBUTION, main, terrace, in(soda, "-3", pcs))))
                .hasMessageContaining("must be positive");

        verifyNothingSavedOrPosted();
    }

    // =====================================================================
    // fixtures and helpers
    // =====================================================================

    private record Posted(UUID storeId, String itemCode, EMovementType type, BigDecimal delta, MovementContext ctx) {}

    private List<Posted> verifyPosted(int expectedCount) {
        ArgumentCaptor<StockBalance> balance = ArgumentCaptor.forClass(StockBalance.class);
        ArgumentCaptor<EMovementType> type = ArgumentCaptor.forClass(EMovementType.class);
        ArgumentCaptor<BigDecimal> delta = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<MovementContext> ctx = ArgumentCaptor.forClass(MovementContext.class);
        verify(ledger, times(expectedCount)).post(balance.capture(), type.capture(), delta.capture(), ctx.capture());
        List<Posted> result = new ArrayList<>();
        for (int i = 0; i < expectedCount; i++) {
            StockBalance b = balance.getAllValues().get(i);
            result.add(new Posted(b.getStore().getId(), b.getItem().getCode(),
                    type.getAllValues().get(i), delta.getAllValues().get(i), ctx.getAllValues().get(i)));
        }
        return result;
    }

    private static void assertPosted(Posted p, Store store, String itemCode, EMovementType type, String delta) {
        assertThat(p.storeId()).isEqualTo(store.getId());
        assertThat(p.itemCode()).isEqualTo(itemCode);
        assertThat(p.type()).isEqualTo(type);
        assertThat(p.delta()).isEqualByComparingTo(delta);
    }

    private StockDocument savedDocument() {
        ArgumentCaptor<StockDocument> captor = ArgumentCaptor.forClass(StockDocument.class);
        verify(documents).save(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private Collection<BalanceKey> lockedKeys() {
        ArgumentCaptor<Collection<BalanceKey>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(ledger).lockAll(captor.capture());
        return captor.getValue();
    }

    private void verifyNothingSavedOrPosted() {
        verify(documents, never()).save(any());
        verifyNoInteractions(ledger);
    }

    private void verifyNothingNumberedSavedOrPosted() {
        verifyNoInteractions(numbers);
        verifyNothingSavedOrPosted();
    }

    private SingleStoreDocumentCommand docCommand(EDocumentType type, Store store, DocumentLineInput... lines) {
        return new SingleStoreDocumentCommand(type, store.getId(), "Acme Ltd", "DN-55", "note text",
                List.of(lines), UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }

    private TransferCommand transfer(ETransferPurpose purpose, Store from, Store to, DocumentLineInput... lines) {
        return new TransferCommand(from.getId(), to.getId(), purpose, "transfer note", List.of(lines), UUID.fromString("00000000-0000-0000-0000-000000000002"));
    }

    private static DocumentLineInput in(StockItem item, String quantity, UnitOfMeasure unit) {
        return new DocumentLineInput(item.getId(), new BigDecimal(quantity), unit.getId());
    }

    private Store store(EStoreType type) {
        return store(type, branch, true);
    }

    private Store store(EStoreType type, HotelBranch storeBranch, boolean active) {
        Store s = new Store();
        s.setName(type.name() + "-store");
        s.setStoreType(type);
        s.setBranch(storeBranch);
        s.setActive(active);
        ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
        knownStores.put(s.getId(), s);
        lenient().when(stores.findById(s.getId())).thenReturn(Optional.of(s));
        return s;
    }

    private static HotelBranch branch() {
        HotelBranch b = new HotelBranch();
        ReflectionTestUtils.setField(b, "id", UUID.randomUUID());
        return b;
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
        knownItems.put(i.getId(), i);
        lenient().when(items.findById(i.getId())).thenReturn(Optional.of(i));
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
