package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EMenuItemType;
import rw.afriteck.pms.enums.EPackageType;
import rw.afriteck.pms.enums.ERecordStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuMasterSummary(
        UUID id,
        String menuItemName,
        EMenuItemType menuItemType,
        EPackageType packageType
) {
}
