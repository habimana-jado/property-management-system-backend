package rw.afriteck.pms.stock.api.view;

import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.api.enums.EDocumentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockDocumentView(UUID id, String documentNo, EDocumentType documentType, ETransferPurpose purpose,
                                EDocumentStatus status, UUID storeId, String storeName,
                                UUID counterpartStoreId, String counterpartStoreName,
                                String supplierName, String externalReference, String note,
                                UUID postedBy, Instant postedAt, List<DocumentLineView> lines) {}
