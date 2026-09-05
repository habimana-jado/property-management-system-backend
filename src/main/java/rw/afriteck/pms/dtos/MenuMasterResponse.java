package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EMenuItemType;
import rw.afriteck.pms.enums.EPackageType;
import rw.afriteck.pms.enums.ERecordStatus;

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
