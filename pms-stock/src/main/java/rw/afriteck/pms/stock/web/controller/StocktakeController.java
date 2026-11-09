package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.stock.api.StocktakeApi;
import rw.afriteck.pms.stock.web.dtos.RecordCountRequest;
import rw.afriteck.pms.stock.web.dtos.StartStocktakeRequest;
import rw.afriteck.pms.stock.web.dtos.StocktakeApprovalResponse;
import rw.afriteck.pms.stock.web.dtos.StocktakeCreatedResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock/stocktakes")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeApi stocktakes;

    @PostMapping
    public ResponseEntity<StocktakeCreatedResponse> start(@Valid @RequestBody StartStocktakeRequest r) {
        UUID id = stocktakes.start(r.storeId(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(new StocktakeCreatedResponse(id));
    }

    @PostMapping("/{id}/counts")
    public ResponseEntity<Void> recordCount(@PathVariable UUID id, @Valid @RequestBody RecordCountRequest r) {
        stocktakes.recordCount(id, r.itemId(), r.countedQuantity(), r.unitId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Void> submit(@PathVariable UUID id) {
        stocktakes.submit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<StocktakeApprovalResponse> approve(@PathVariable UUID id) {
        UUID documentId = stocktakes.approve(id, null);
        return ResponseEntity.ok(new StocktakeApprovalResponse(documentId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        stocktakes.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
