package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePOSProductCategoryRequest(
        @NotNull UUID hotelBranchId,
        @NotBlank String categoryName
) {
}
