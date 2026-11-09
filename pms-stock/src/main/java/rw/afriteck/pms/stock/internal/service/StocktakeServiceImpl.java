package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.StocktakeApi;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class StocktakeServiceImpl implements StocktakeApi {

    private static final List<EStocktakeStatus> OPEN =
            List.of(EStocktakeStatus.DRAFT, EStocktakeStatus.COUNTING, EStocktakeStatus.SUBMITTED);

    private final StockTakeRepository stocktakes;
    private final StoreRepository stores;
    private final StockItemRepository items;
    private final UnitOfMeasureRepository units;
    private final StockBalanceRepository balances;
    private final StockDocumentRepository documents;
    private final UnitOfConversionService conversion;
    private final StockOperations operations;

    @Override
    @Transactional
    public UUID start(UUID storeId, UUID startedBy) {
        Store store = stores.findById(storeId).orElseThrow(() -> new StockRuleViolationException("Unknown store"));
        if (stocktakes.existsByStoreIdAndStatusIn(storeId, OPEN)) {
            throw new StockRuleViolationException("This store already has an open stocktake");
        }
        Stocktake st = new Stocktake();
        st.setStore(store);
        st.setStatus(EStocktakeStatus.COUNTING);
        st.setStartedBy(startedBy);
        st.setStartedAt(Instant.now());
        for (StockBalance b : balances.findByStoreId(storeId)) {      // initial lines, expected refreshed per count
            StocktakeLine line = new StocktakeLine();
            line.setItem(b.getItem());
            line.setExpectedQuantity(b.getQuantityOnHand());
            st.addLine(line);
        }
        return stocktakes.save(st).getId();                           // unique open_store_key is the backstop
    }

    @Override
    @Transactional
    public void recordCount(UUID stocktakeId, UUID itemId, BigDecimal counted, UUID unitId) {
        Stocktake st = load(stocktakeId);
        require(st.getStatus() == EStocktakeStatus.COUNTING, "Stocktake is not in counting state");
        StockItem item = items.findById(itemId).orElseThrow(() -> new StockRuleViolationException("Unknown item"));
        UnitOfMeasure unit = units.findById(unitId).orElseThrow(() -> new StockRuleViolationException("Unknown unit"));
        BigDecimal base = conversion.toBase(item, unit, counted);
        require(base.signum() >= 0, "Counted quantity cannot be negative");

        StocktakeLine line = st.getLines().stream()
                .filter(l -> l.getItem().getId().equals(itemId)).findFirst()
                .orElseGet(() -> { StocktakeLine l = new StocktakeLine(); l.setItem(item); st.addLine(l); return l; });

        // expected = live balance at the moment of counting (sales keep running during a count)
        line.setExpectedQuantity(balances.findByStoreIdAndItemId(st.getStore().getId(), itemId)
                .map(StockBalance::getQuantityOnHand).orElse(BigDecimal.ZERO));
        line.setCountedQuantity(base);
    }

    @Override
    @Transactional
    public void submit(UUID stocktakeId) {
        Stocktake st = load(stocktakeId);
        require(st.getStatus() == EStocktakeStatus.COUNTING, "Stocktake is not in counting state");
        require(st.getLines().stream().anyMatch(l -> l.getCountedQuantity() != null), "Nothing has been counted");
        st.setStatus(EStocktakeStatus.SUBMITTED);
    }

    @Override
    @Transactional
    public UUID approve(UUID stocktakeId, UUID approvedBy) {
        Stocktake st = stocktakes.findForUpdate(stocktakeId)           // blocks a double approval
                .orElseThrow(() -> new StockRuleViolationException("Unknown stocktake"));
        require(st.getStatus() == EStocktakeStatus.SUBMITTED, "Stocktake is not submitted");

        // uncounted lines (variance == null) produce no adjustment
        List<DocumentLineInput> adjustments = st.getLines().stream()
                .filter(l -> l.getVariance() != null && l.getVariance().signum() != 0)
                .map(l -> new DocumentLineInput(l.getItem().getId(), l.getVariance(),
                        l.getItem().getBaseUnit().getId()))
                .toList();

        UUID documentId = null;
        if (!adjustments.isEmpty()) {
            documentId = operations.postDocument(new SingleStoreDocumentCommand(
                    EDocumentType.ADJUSTMENT, st.getStore().getId(), null,
                    "STOCKTAKE:" + st.getId(), "Stocktake approval", adjustments, approvedBy));
            st.setAdjustmentDocument(documents.getReferenceById(documentId));
        }
        st.setStatus(EStocktakeStatus.APPROVED);
        st.setApprovedBy(approvedBy);
        st.setApprovedAt(Instant.now());
        return documentId;
    }

    @Override
    @Transactional
    public void cancel(UUID stocktakeId) {
        Stocktake st = load(stocktakeId);
        require(st.getStatus() != EStocktakeStatus.APPROVED, "An approved stocktake cannot be cancelled");
        st.setStatus(EStocktakeStatus.CANCELLED);
    }

    private Stocktake load(UUID id) {
        return stocktakes.findById(id).orElseThrow(() -> new StockRuleViolationException("Unknown stocktake"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}
