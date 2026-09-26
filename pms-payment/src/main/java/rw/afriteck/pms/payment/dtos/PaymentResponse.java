package rw.afriteck.pms.payment.dtos;

import rw.afriteck.pms.payment.enums.EPaymentStatus;

import java.math.BigDecimal;

public record PaymentResponse(
        EPaymentStatus status,
        BigDecimal amount
) {}
