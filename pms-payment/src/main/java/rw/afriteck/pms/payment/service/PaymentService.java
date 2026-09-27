package rw.afriteck.pms.payment.service;

import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.payment.dtos.PaymentResult;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentResult processPayment(ProcessPaymentRequest paymentRequest);

    BigDecimal sumPaidAmount(UUID payableId, EPayableType payableType);
}
