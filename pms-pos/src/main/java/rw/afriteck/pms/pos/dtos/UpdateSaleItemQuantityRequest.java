package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateSaleItemQuantityRequest(
        @NotNull @Positive Integer quantity
) {
}
