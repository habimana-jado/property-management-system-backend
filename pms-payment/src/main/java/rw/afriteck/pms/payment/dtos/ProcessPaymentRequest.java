package rw.afriteck.pms.payment.dtos;

import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
        UUID payableId,
        EPayableType payableType,
        BigDecimal amountOwed,
        BigDecimal amount,
        EPaymentMethod method,
        String reference
) {
}
