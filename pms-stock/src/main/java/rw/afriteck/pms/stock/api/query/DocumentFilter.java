package rw.afriteck.pms.stock.api.query;

import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EDocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentFilter(UUID branchId, UUID storeId, EDocumentType type, EDocumentStatus status,
                             Instant from, Instant to) {}
