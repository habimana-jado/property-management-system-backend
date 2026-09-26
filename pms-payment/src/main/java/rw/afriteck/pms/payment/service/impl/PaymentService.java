package rw.afriteck.pms.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.payment.dtos.PaymentResponse;
import rw.afriteck.pms.payment.dtos.RecordPaymentRequest;
import rw.afriteck.pms.payment.enums.EPaymentStatus;
import rw.afriteck.pms.payment.mapper.PaymentMapper;
import rw.afriteck.pms.payment.model.Payment;
import rw.afriteck.pms.payment.repository.PaymentRepo;
import rw.afriteck.pms.payment.service.IPaymentService;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PaymentRepo paymentRepo;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse processPayment(RecordPaymentRequest paymentRequest) {

        BigDecimal alreadyPaid = paymentRepo.sumActiveAmountByBillId(paymentRequest.sourceReferenceId());
        BigDecimal remainingBalance = paymentRequest.billTotalAmount().subtract(alreadyPaid);

        if (paymentRequest.amount().compareTo(remainingBalance) > 0) {
            throw new BusinessRuleViolationException("OVERPAYMENT", "Payment exceeds remaining balance");
        }

        Payment payment = new Payment();
        payment.setSourceReferenceId(paymentRequest.sourceReferenceId());
        payment.setSourceType(paymentRequest.sourceType());
        payment.setAmount(paymentRequest.amount());
        payment.setMethod(paymentRequest.method());
        payment.setStatus(EPaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment.setReference(paymentRequest.reference());

        return paymentMapper.toResponse(paymentRepo.save(payment));
    }
}
