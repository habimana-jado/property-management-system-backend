package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.internal.dtos.BalanceKey;
import rw.afriteck.pms.stock.internal.dtos.MovementContext;
import rw.afriteck.pms.stock.api.enums.EDocumentStatus;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.internal.enums.EStoreType;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.StockDocumentRepository;
import rw.afriteck.pms.stock.internal.repository.StockItemRepository;
import rw.afriteck.pms.stock.internal.repository.StoreRepository;
import rw.afriteck.pms.stock.internal.repository.UnitOfMeasureRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class StockOperationsImpl  implements StockOperations {

    private final StoreRepository stores;
    private final StockItemRepository items;
    private final UnitOfMeasureRepository units;
    private final StockDocumentRepository documents;
    private final DocumentNumberService numbers;
    private final UnitOfConversionService conversion;
    private final StockLedger ledger;

    // Authorization (store-manager role from pms-auth) belongs on the controller or here
    // via @PreAuthorize; the role name depends on your pms-auth, so it is not hard-coded.

    @Override
    @Transactional
    public UUID postDocument(SingleStoreDocumentCommand cmd) {
        Store store = activeStore(cmd.storeId());
        validateStoreForType(cmd.type(), store);

        StockDocument doc = newDocument(cmd.type(), store, null, null, cmd.performedBy());
        doc.setSupplierName(cmd.supplierName());
        doc.setExternalReference(cmd.externalReference());
        doc.setNote(cmd.note());
        addLines(doc, cmd.lines(), cmd.type() == EDocumentType.ADJUSTMENT);
        documents.save(doc);

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(doc.getLines().stream()
                .map(l -> new BalanceKey(store.getId(), l.getItem().getId())).toList());

        EMovementType movementType = movementTypeFor(cmd.type());
        for (StockDocumentLine line : doc.getLines()) {
            StockBalance balance = locked.get(new BalanceKey(store.getId(), line.getItem().getId()));
            ledger.post(balance, movementType, signedDelta(cmd.type(), line.getQuantity()),
                    MovementContext.forDocument(line, cmd.note(), cmd.performedBy()));
        }
        return doc.getId();
    }

    @Override
    @Transactional
    public UUID transfer(TransferCommand cmd) {
        Store from = activeStore(cmd.fromStoreId());
        Store to = activeStore(cmd.toStoreId());
        validateTransfer(cmd.purpose(), from, to);

        StockDocument doc = newDocument(EDocumentType.TRANSFER, from, to, cmd.purpose(), cmd.performedBy());
        doc.setNote(cmd.note());
        addLines(doc, cmd.lines(), false);
        documents.save(doc);

        List<BalanceKey> keys = new ArrayList<>();
        for (StockDocumentLine l : doc.getLines()) {
            keys.add(new BalanceKey(from.getId(), l.getItem().getId()));
            keys.add(new BalanceKey(to.getId(), l.getItem().getId()));
        }
        Map<BalanceKey, StockBalance> locked = ledger.lockAll(keys);   // sorted across both stores

        for (StockDocumentLine l : doc.getLines()) {
            MovementContext ctx = MovementContext.forDocument(l, cmd.note(), cmd.performedBy());
            ledger.post(locked.get(new BalanceKey(from.getId(), l.getItem().getId())),
                    EMovementType.TRANSFER_OUT, l.getQuantity().negate(), ctx);
            ledger.post(locked.get(new BalanceKey(to.getId(), l.getItem().getId())),
                    EMovementType.TRANSFER_IN, l.getQuantity(), ctx);
        }
        return doc.getId();
    }

    // ---- rules ----
    private void validateTransfer(ETransferPurpose purpose, Store from, Store to) {
        require(!from.getId().equals(to.getId()), "Source and destination must differ");
        require(from.getBranch().getId().equals(to.getBranch().getId()), "Stores must belong to the same branch");
        switch (purpose) {
            case DISTRIBUTION -> require(from.getStoreType() == EStoreType.MAIN
                    && to.getStoreType() == EStoreType.DEPARTMENT, "Distribution goes from the main store to a department store");
            case RETURN -> require(to.getStoreType() == EStoreType.MAIN
                    && from.getStoreType() == EStoreType.DEPARTMENT, "Return goes from a department store to the main store");
            case DAMAGE_REPORT -> require(to.getStoreType() == EStoreType.DAMAGED
                    && from.getStoreType() != EStoreType.DAMAGED, "Damage reports go into the damaged store");
        }
    }

    private void validateStoreForType(EDocumentType type, Store store) {
        switch (type) {
            case RECEIPT, SUPPLIER_RETURN -> require(store.getStoreType() == EStoreType.MAIN, "Main store only");
            case WRITE_OFF -> require(store.getStoreType() == EStoreType.DAMAGED, "Write-offs apply to the damaged store");
            case CONSUMPTION, CUSTOMER_RETURN -> require(store.getStoreType() != EStoreType.DAMAGED, "Not allowed on the damaged store");
            case TRANSFER -> throw new StockRuleViolationException("Use transfer() for transfers");
            default -> { }                                   // ADJUSTMENT, OPENING_BALANCE: any store
        }
    }

    // ---- helpers ----
    private void addLines(StockDocument doc, List<DocumentLineInput> inputs, boolean signedAllowed) {
        require(inputs != null && !inputs.isEmpty(), "A document needs at least one line");
        for (DocumentLineInput in : inputs) {
            StockItem item = items.findById(in.itemId()).orElseThrow(
                    () -> new StockRuleViolationException("Unknown stock item"));
            require(item.isActive(), "Stock item is inactive: " + item.getName());
            UnitOfMeasure unit = units.findById(in.unitId()).orElseThrow(
                    () -> new StockRuleViolationException("Unknown unit"));
            BigDecimal base = conversion.toBase(item, unit, in.quantity());
            require(base.signum() != 0, "Quantity rounds to zero for " + item.getName());
            require(signedAllowed || base.signum() > 0, "Quantity must be positive for " + item.getName());

            StockDocumentLine line = new StockDocumentLine();
            line.setItem(item);
            line.setQuantity(base);                         // adjustments keep their sign
            line.setEnteredQuantity(in.quantity());
            line.setEnteredUnit(unit);
            doc.addLine(line);
        }
    }

    private StockDocument newDocument(EDocumentType type, Store store, Store counterpart,
                                      ETransferPurpose purpose, UUID by) {
        StockDocument d = new StockDocument();
        d.setDocumentType(type);
        d.setDocumentNo(numbers.next(store.getBranch(), type));
        d.setStore(store);
        d.setCounterpartStore(counterpart);
        d.setPurpose(purpose);
        d.setStatus(EDocumentStatus.POSTED);
        d.setPostedBy(by);
        d.setPostedAt(Instant.now());
        return d;
    }

    private Store activeStore(UUID id) {
        Store s = stores.findById(id).orElseThrow(() -> new StockRuleViolationException("Unknown store"));
        require(s.isActive(), "Store is inactive: " + s.getName());
        return s;
    }

    private static EMovementType movementTypeFor(EDocumentType t) {
        return switch (t) {
            case RECEIPT -> EMovementType.RECEIPT;
            case OPENING_BALANCE -> EMovementType.OPENING_BALANCE;
            case ADJUSTMENT -> EMovementType.ADJUSTMENT;
            case WRITE_OFF -> EMovementType.DAMAGE_WRITE_OFF;
            case SUPPLIER_RETURN -> EMovementType.SUPPLIER_RETURN;
            case CUSTOMER_RETURN -> EMovementType.CUSTOMER_RETURN;
            case CONSUMPTION -> EMovementType.CONSUMPTION;
            case TRANSFER -> throw new IllegalArgumentException("Transfers use TRANSFER_OUT/IN");
        };
    }

    private static BigDecimal signedDelta(EDocumentType t, BigDecimal q) {
        return switch (t) {
            case RECEIPT, OPENING_BALANCE, CUSTOMER_RETURN -> q;
            case WRITE_OFF, SUPPLIER_RETURN, CONSUMPTION -> q.negate();
            case ADJUSTMENT -> q;                           // already signed
            case TRANSFER -> throw new IllegalArgumentException();
        };
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}
