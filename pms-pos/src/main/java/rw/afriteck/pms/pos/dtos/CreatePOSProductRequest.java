package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePOSProductRequest(
        @NotNull UUID hotelBranchId,
        @NotNull UUID categoryId,
        @NotBlank String sku,
        @NotBlank String name,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal unitPrice,
        @NotNull boolean trackInventory
) {
}
