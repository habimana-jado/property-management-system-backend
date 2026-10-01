package rw.afriteck.pms.stock.api.command;

import rw.afriteck.pms.stock.api.enums.EDocumentType;

import java.util.List;
import java.util.UUID;

public record SingleStoreDocumentCommand(
        EDocumentType type,
        UUID storeId,
        String supplierName,
        String externalReference,
        String note,
        List<DocumentLineInput> lines,
        UUID performedBy
) {
}
