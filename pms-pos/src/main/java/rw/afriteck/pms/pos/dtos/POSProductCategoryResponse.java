package rw.afriteck.pms.pos.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

public record POSProductCategoryResponse(
        UUID id,
        UUID hotelBranchId,
        String categoryName,
        ERecordStatus status) {
}
