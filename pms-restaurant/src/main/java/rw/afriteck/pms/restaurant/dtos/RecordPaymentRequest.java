package rw.afriteck.pms.restaurant.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.enums.EPayableType;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull EPaymentMethod method,
        String reference
) {
}
