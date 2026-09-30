package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddSaleItemRequest(
        @NotNull UUID productId,
        @NotNull @Positive Integer quantity
) {
}
