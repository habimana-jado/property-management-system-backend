package rw.afriteck.pms.stock.api.view;

import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;

import java.time.Instant;
import java.util.UUID;

public record StocktakeSummary(UUID id, UUID storeId, String storeName, EStocktakeStatus status,
                               UUID startedBy, Instant startedAt, Instant approvedAt, UUID adjustmentDocumentId) {}
