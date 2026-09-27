package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotBlank;

public record UpdatePOSProductCategoryRequest(
        @NotBlank String categoryName) {
}
