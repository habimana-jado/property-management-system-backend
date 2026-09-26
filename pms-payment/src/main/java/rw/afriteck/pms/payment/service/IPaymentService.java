package rw.afriteck.pms.payment.service;

import rw.afriteck.pms.payment.dtos.PaymentResponse;
import rw.afriteck.pms.payment.dtos.RecordPaymentRequest;

public interface IPaymentService {
    PaymentResponse processPayment(RecordPaymentRequest paymentRequest);
}
