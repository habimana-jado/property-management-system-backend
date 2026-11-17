package rw.afriteck.pms.credit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.credit.dtos.*;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.CreditCustomerRepository;
import rw.afriteck.pms.credit.repository.ReceivableRepository;
import rw.afriteck.pms.payment.dtos.PaymentResult;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditService {

    private static final List<EReceivableStatus> ACTIVE =
            List.of(EReceivableStatus.OUTSTANDING, EReceivableStatus.PARTIALLY_SETTLED);

    private final CreditCustomerRepository customerRepository;
    private final ReceivableRepository receivableRepository;
    private final PaymentService paymentService; // from pms-payment

    /** Called by domain modules (restaurant, pos) when a sale is closed on credit. */
    @Transactional
    public ReceivableResponse grantCredit(GrantCreditRequest request) {

        // lock the customer row: serializes limit checks and repayments per customer
        CreditCustomer customer = customerRepository.findByIdForUpdate(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        if (!customer.isActive() || !customer.isCreditAllowed()) {
            throw new BusinessRuleViolationException("CREDIT_NOT_ALLOWED", "This customer is not allowed credit");
        }
        if (request.dueDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleViolationException("INVALID_DUE_DATE", "Due date cannot be in the past");
        }
        if (receivableRepository.existsBySourceTypeAndSourceId(request.sourceType(), request.sourceId())) {
            throw new BusinessRuleViolationException("CREDIT_ALREADY_GRANTED", "Credit was already granted for this sale");
        }

        BigDecimal outstanding = receivableRepository.sumOutstandingByCustomer(customer.getId(), ACTIVE);
        if (outstanding.add(request.amount()).compareTo(customer.getCreditLimit()) > 0) {
            throw new BusinessRuleViolationException("CREDIT_LIMIT_EXCEEDED",
                    "Credit limit exceeded. Available: " + customer.getCreditLimit().subtract(outstanding));
        }

        Receivable receivable = new Receivable();
        receivable.setCustomer(customer);
        receivable.setSourceType(request.sourceType());
        receivable.setSourceId(request.sourceId());
        receivable.setSourceReference(request.sourceReference());
        receivable.setOriginalAmount(request.amount());
        receivable.setDueDate(request.dueDate());
        receivable.setIssuedAt(Instant.now());
        receivableRepository.save(receivable);

        return toResponse(receivable);
    }

    /** Customer pays an amount toward their tab; applied oldest debt first. */
    @Transactional
    public RepayDebtResponse repay(UUID customerId, RepayDebtRequest request) {

        CreditCustomer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        List<Receivable> open = receivableRepository
                .findByCustomerIdAndStatusInOrderByIssuedAtAsc(customer.getId(), ACTIVE);

        if (open.isEmpty()) {
            throw new BusinessRuleViolationException("NO_OUTSTANDING_DEBT", "Customer has no outstanding debt");
        }

        BigDecimal totalOutstanding = open.stream()
                .map(Receivable::outstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.amount().compareTo(totalOutstanding) > 0) {
            throw new BusinessRuleViolationException("OVERPAYMENT", "Amount exceeds total outstanding debt");
        }

        BigDecimal toDistribute = request.amount();
        List<ReceivableAllocationResponse> allocations = new ArrayList<>();

        for (Receivable receivable : open) {
            if (toDistribute.signum() == 0) break;

            BigDecimal applied = toDistribute.min(receivable.outstandingBalance());

            PaymentResult result = paymentService.processPayment(new ProcessPaymentRequest(
                    receivable.getId(),
                    EPayableType.RECEIVABLE,
                    receivable.getOriginalAmount(), // amountOwed (Option A)
                    applied,
                    request.method(),
                    request.reference()
            ));

            receivable.setAmountPaid(receivable.getAmountPaid().add(applied));
            receivable.setStatus(result.fullySettled()
                    ? EReceivableStatus.SETTLED
                    : EReceivableStatus.PARTIALLY_SETTLED);
            // managed entity: dirty checking persists this

            toDistribute = toDistribute.subtract(applied);
            allocations.add(new ReceivableAllocationResponse(
                    receivable.getId(), receivable.getSourceReference(),
                    applied, receivable.outstandingBalance(), result.fullySettled()));
        }

        return new RepayDebtResponse(
                request.amount(),
                totalOutstanding.subtract(request.amount()),
                allocations
        );
    }

    @Transactional(readOnly = true)
    public List<ReceivableResponse> listOutstanding(UUID customerId) {
        return receivableRepository.findByCustomerIdAndStatusInOrderByIssuedAtAsc(customerId, ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public WriteOffResponse writeOff(UUID receivableId, WriteOffRequest request) {

        // lock the customer BEFORE loading the receivable, so we see committed state
        // from any repayment that was in flight
        UUID customerId = receivableRepository.findCustomerIdById(receivableId)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable", receivableId));

        CreditCustomer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));

        Receivable receivable = receivableRepository.findById(receivableId)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable", receivableId));

        if (!receivable.isActive()) {
            throw new BusinessRuleViolationException("INVALID_RECEIVABLE_STATE",
                    "Only an outstanding debt can be written off");
        }

        BigDecimal amount = receivable.outstandingBalance(); // compute BEFORE changing status

        receivable.setStatus(EReceivableStatus.WRITTEN_OFF);
        receivable.setWrittenOffAmount(amount);
        receivable.setWriteOffReason(request.reason());
        receivable.setWrittenOffBy(request.approvedBy());
        receivable.setWrittenOffAt(Instant.now());

        customer.setCreditAllowed(false); // business default, see note above

        return new WriteOffResponse(receivable.getId(), customer.getFullName(),
                receivable.getSourceReference(), amount,
                request.reason(), request.approvedBy(), receivable.getWrittenOffAt());
    }

    private ReceivableResponse toResponse(Receivable r) {
        return new ReceivableResponse(r.getId(), r.getCustomer().getId(), r.getCustomer().getFullName(),
                r.getSourceType(), r.getSourceReference(), r.getOriginalAmount(), r.getAmountPaid(),
                r.outstandingBalance(), r.getDueDate(), r.getStatus(), r.isOverdue());
    }
}
