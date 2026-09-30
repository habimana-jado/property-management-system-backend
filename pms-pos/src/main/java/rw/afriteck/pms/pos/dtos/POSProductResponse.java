package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record POSProductResponse(
        UUID id,
        UUID hotelBranchId,
        UUID categoryId,
        String categoryName,
        String sku,
        String name,
        String barcode,
        String imageUrl,
        BigDecimal unitPrice,
        boolean trackInventory,
        ERecordStatus status
) {
}
