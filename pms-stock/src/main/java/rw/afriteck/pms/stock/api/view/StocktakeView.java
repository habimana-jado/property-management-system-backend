package rw.afriteck.pms.stock.api.view;

import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StocktakeView(UUID id, UUID storeId, String storeName, EStocktakeStatus status,
                            UUID startedBy, Instant startedAt, UUID approvedBy, Instant approvedAt,
                            String note, UUID adjustmentDocumentId, List<StocktakeLineView> lines) {}
