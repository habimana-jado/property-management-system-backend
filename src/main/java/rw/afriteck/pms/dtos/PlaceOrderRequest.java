package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID menuItemId,
        @NotNull @Positive Integer transactionQuantity,
        String remarks
) {
}
