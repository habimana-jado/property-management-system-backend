package rw.afriteck.pms.credit.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.common.enums.EPaymentMethod;

import java.math.BigDecimal;

public record RepayDebtRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull EPaymentMethod method,
        String reference
) {}
