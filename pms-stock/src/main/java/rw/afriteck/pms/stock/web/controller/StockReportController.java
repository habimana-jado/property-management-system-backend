package rw.afriteck.pms.stock.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.stock.api.StockQueryApi;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.query.DocumentFilter;
import rw.afriteck.pms.stock.api.query.MovementFilter;
import rw.afriteck.pms.stock.api.query.StockOnHandFilter;
import rw.afriteck.pms.stock.api.query.StocktakeFilter;
import rw.afriteck.pms.stock.api.view.*;
import rw.afriteck.pms.stock.api.enums.EDocumentStatus;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.web.security.StockPermissions;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock")
@PreAuthorize(StockPermissions.STORE_MANAGER)
@RequiredArgsConstructor
public class StockReportController {

    private final StockQueryApi queries;

    @GetMapping("/on-hand")
    public PageResult<StockOnHandRow> onHand(
            @RequestParam UUID branchId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeZero,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return queries.stockOnHand(new StockOnHandFilter(branchId, storeId, categoryId, search, includeZero), page, size);
    }

    @GetMapping("/movements")
    public PageResult<MovementRow> movements(
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) Set<EMovementType> type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID documentId,
            @RequestParam(required = false) EMovementSourceType sourceType,
            @RequestParam(required = false) UUID sourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return queries.movements(new MovementFilter(storeId, itemId, type, from, to, documentId, sourceType, sourceId),
                page, size);
    }

    @GetMapping("/low-stock")
    public List<LowStockAlert> lowStock(@RequestParam UUID branchId) {
        return queries.lowStock(branchId);
    }

    @GetMapping("/documents")
    public PageResult<StockDocumentSummary> documents(
            @RequestParam UUID branchId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) EDocumentType type,
            @RequestParam(required = false) EDocumentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return queries.documents(new DocumentFilter(branchId, storeId, type, status, from, to), page, size);
    }

    @GetMapping("/documents/{id}")
    public StockDocumentView document(@PathVariable UUID id) {
        return queries.document(id);
    }

    @GetMapping("/stocktakes")
    public PageResult<StocktakeSummary> stocktakes(
            @RequestParam UUID branchId,
            @RequestParam(required = false) UUID storeId,
            @RequestParam(required = false) EStocktakeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return queries.stocktakes(new StocktakeFilter(branchId, storeId, status), page, size);
    }

    @GetMapping("/stocktakes/{id}")
    public StocktakeView stocktake(@PathVariable UUID id) {
        return queries.stocktake(id);
    }
}