package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.restaurant.enums.EMenuItemType;
import rw.afriteck.pms.restaurant.enums.EPackageType;

import java.util.UUID;

public record MenuMasterSummary(
        UUID id,
        String menuItemName,
        EMenuItemType menuItemType,
        EPackageType packageType
) {
}
