package rw.afriteck.pms.pos.service;

import rw.afriteck.pms.pos.dtos.POSInventoryResponse;
import rw.afriteck.pms.pos.dtos.RestockRequest;
import rw.afriteck.pms.pos.dtos.StockAdjustmentRequest;
import rw.afriteck.pms.pos.dtos.StockMovementResponse;

import java.util.UUID;

public interface POSInventoryService {

    StockMovementResponse restock(RestockRequest request);

    StockMovementResponse adjust(StockAdjustmentRequest request);

    POSInventoryResponse getStockLevel(UUID productId, UUID hotelBranchId);
}
