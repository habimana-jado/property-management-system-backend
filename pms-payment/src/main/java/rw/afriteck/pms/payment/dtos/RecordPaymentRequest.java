package rw.afriteck.pms.payment.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.payment.enums.EPaymentMethod;
import rw.afriteck.pms.payment.enums.EPaymentSourceType;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull EPaymentMethod method,
        @NotBlank UUID sourceReferenceId,
        @NotBlank EPaymentSourceType sourceType,
        String reference,
        @NotNull BigDecimal billTotalAmount,
        BigDecimal billTotalPaidAmount
) {
}
