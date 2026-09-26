package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.restaurant.enums.EMenuItemType;
import rw.afriteck.pms.restaurant.enums.EPackageType;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuMasterResponse(
        UUID id,
        String menuItemName,
        BigDecimal unitPrice,
        EMenuItemType menuItemType,
        EPackageType packageType,
        ERecordStatus status,
        UUID menuCategoryId
) {
}
