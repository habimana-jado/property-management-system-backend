package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.restaurant.enums.EMenuItemType;
import rw.afriteck.pms.restaurant.enums.EPackageType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMenuMasterRequest(
        @NotBlank String menuItemName,
        @NotNull BigDecimal unitPrice,
        @NotNull EMenuItemType menuItemType,
        @NotNull EPackageType packageType,
        @NotNull UUID menuCategoryId
) {
}
