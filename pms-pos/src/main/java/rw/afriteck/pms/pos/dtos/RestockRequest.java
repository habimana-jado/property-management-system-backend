package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record RestockRequest(
        @NotNull UUID productId,
        @NotNull UUID hotelBranchId,
        @NotNull @Positive Integer quantity,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal purchasePrice,
        String reference // e.g. supplier invoice number

) {
}
