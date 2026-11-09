package rw.afriteck.pms.stock.api.view;

import org.w3c.dom.DocumentType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.api.enums.EDocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record StockDocumentSummary(UUID id, String documentNo, DocumentType documentType, ETransferPurpose purpose,
                                   EDocumentStatus status, UUID storeId, String storeName,
                                   UUID counterpartStoreId, String counterpartStoreName,
                                   UUID postedBy, Instant postedAt) {}
