package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.common.security.CurrentUserService; // adjust to the real package in pms-common
import rw.afriteck.pms.stock.api.StocktakeApi;
import rw.afriteck.pms.stock.web.dtos.RecordCountRequest;
import rw.afriteck.pms.stock.web.dtos.StartStocktakeRequest;
import rw.afriteck.pms.stock.web.dtos.StocktakeApprovalResponse;
import rw.afriteck.pms.stock.web.dtos.StocktakeCreatedResponse;
import rw.afriteck.pms.stock.web.security.StockPermissions;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock/stocktakes")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeApi stocktakes;
    private final CurrentUserService currentUser;

    @PostMapping
    @PreAuthorize(StockPermissions.Has.STOCKTAKE_COUNT)
    public ResponseEntity<StocktakeCreatedResponse> start(@Valid @RequestBody StartStocktakeRequest r) {
        UUID id = stocktakes.start(r.storeId(), currentUser.requireCurrentStaffId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new StocktakeCreatedResponse(id));
    }

    @PostMapping("/{id}/counts")
    @PreAuthorize(StockPermissions.Has.STOCKTAKE_COUNT)
    public ResponseEntity<Void> recordCount(@PathVariable UUID id, @Valid @RequestBody RecordCountRequest r) {
        stocktakes.recordCount(id, r.itemId(), r.countedQuantity(), r.unitId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize(StockPermissions.Has.STOCKTAKE_COUNT)
    public ResponseEntity<Void> submit(@PathVariable UUID id) {
        stocktakes.submit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize(StockPermissions.Has.STOCKTAKE_APPROVE)
    public ResponseEntity<StocktakeApprovalResponse> approve(@PathVariable UUID id) {
        UUID documentId = stocktakes.approve(id, currentUser.requireCurrentStaffId());
        return ResponseEntity.ok(new StocktakeApprovalResponse(documentId));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize(StockPermissions.Has.STOCKTAKE_COUNT)
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        stocktakes.cancel(id);
        return ResponseEntity.noContent().build();
    }
}