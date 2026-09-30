package rw.afriteck.pms.pos.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import rw.afriteck.pms.common.enums.EPaymentMethod;

import java.math.BigDecimal;

public record TenderRequest(
        @NotNull EPaymentMethod method,
        @NotNull @Positive BigDecimal amount,
        String reference
) {
}
