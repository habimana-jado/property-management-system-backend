package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.enums.EPaymentMethod;

import java.math.BigDecimal;

public record RecordPaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull EPaymentMethod method,
        String reference
) {
}
