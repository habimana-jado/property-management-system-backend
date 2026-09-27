package rw.afriteck.pms.payment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.payment.dtos.PaymentResult;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.payment.enums.EPaymentStatus;
import rw.afriteck.pms.payment.mapper.PaymentMapper;
import rw.afriteck.pms.payment.model.Payment;
import rw.afriteck.pms.payment.repository.PaymentRepo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService implements rw.afriteck.pms.payment.service.PaymentService {

    private final PaymentRepo paymentRepo;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResult processPayment(ProcessPaymentRequest paymentRequest) {

        BigDecimal alreadyPaid = paymentRepo.sumActiveAmountByPayableId(paymentRequest.payableId(), paymentRequest.payableType());
        BigDecimal remainingBalance = paymentRequest.amountOwed().subtract(alreadyPaid);

        if (paymentRequest.amount().compareTo(remainingBalance) > 0) {
            throw new BusinessRuleViolationException("OVERPAYMENT", "Payment exceeds remaining balance");
        }

        Payment payment = new Payment();
        payment.setPayableId(paymentRequest.payableId());
        payment.setPayableType(paymentRequest.payableType());
        payment.setAmount(paymentRequest.amount());
        payment.setMethod(paymentRequest.method());
        payment.setStatus(EPaymentStatus.COMPLETED);
        payment.setPaidAt(Instant.now());
        payment.setReference(paymentRequest.reference());
        paymentRepo.save(payment);

        BigDecimal newTotalPaid = alreadyPaid.add(paymentRequest.amount());
        boolean fullySettled = newTotalPaid.compareTo(paymentRequest.amountOwed()) == 0;
        BigDecimal remaining = paymentRequest.amountOwed().subtract(newTotalPaid);

        return new PaymentResult(fullySettled, remaining, payment.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumPaidAmount(UUID payableId, EPayableType payableType) {
        return paymentRepo.sumActiveAmountByPayableId(payableId, payableType);
    }
}
