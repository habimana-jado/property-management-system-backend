package rw.afriteck.pms.pos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.pos.dtos.POSInventoryResponse;
import rw.afriteck.pms.pos.dtos.RestockRequest;
import rw.afriteck.pms.pos.dtos.StockAdjustmentRequest;
import rw.afriteck.pms.pos.dtos.StockMovementResponse;
import rw.afriteck.pms.pos.service.POSInventoryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/pos-inventory")
@RequiredArgsConstructor
public class POSInventoryController {

    private final POSInventoryService inventoryService;

    @PostMapping("/restock")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse restock(@Valid @RequestBody RestockRequest request) {
        return inventoryService.restock(request);
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public StockMovementResponse adjust(@Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryService.adjust(request);
    }

    @GetMapping
    public POSInventoryResponse getStockLevel(@RequestParam UUID productId, @RequestParam UUID hotelBranchId) {
        return inventoryService.getStockLevel(productId, hotelBranchId);
    }
}
