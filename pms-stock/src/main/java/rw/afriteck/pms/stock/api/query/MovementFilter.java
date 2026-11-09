package rw.afriteck.pms.stock.api.query;

import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EMovementType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MovementFilter(UUID storeId, UUID itemId, Set<EMovementType> types, Instant from, Instant to,
                             UUID documentId, EMovementSourceType sourceType, UUID sourceId) {}
