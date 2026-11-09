package rw.afriteck.pms.stock.api.view;

import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EMovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementRow(UUID movementId, Instant occurredAt, UUID storeId, String storeName,
                          UUID itemId, String itemCode, String itemName, EMovementType movementType,
                          BigDecimal quantity, BigDecimal balanceAfter, UUID documentId, String documentNo,
                          EMovementSourceType sourceType, UUID sourceId, UUID sourceLineId, UUID reversalOfId,
                          String reason, String note, UUID performedBy) {}
