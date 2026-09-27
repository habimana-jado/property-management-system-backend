package rw.afriteck.pms.payment.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResult(
        boolean fullySettled,
        BigDecimal remainingBalance,
        UUID paymentId
) {
}
