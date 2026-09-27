package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.pos.enums.EStockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        EStockMovementType movementType,
        Integer quantity,
        BigDecimal purchasePriceAtRestock,
        Integer newQuantityOnHand,
        LocalDateTime createdAt
) {
}
