package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePriceRequest(
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal unitPrice
) {
}
