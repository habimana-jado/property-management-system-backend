package rw.afriteck.pms.credit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.credit.dtos.RepayDebtRequest;
import rw.afriteck.pms.credit.dtos.RepayDebtResponse;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.CreditCustomerRepository;
import rw.afriteck.pms.credit.repository.ReceivableRepository;
import rw.afriteck.pms.credit.service.CreditService;
import rw.afriteck.pms.payment.dtos.PaymentResult;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceRepayTest {

    @Mock
    CreditCustomerRepository customerRepository;
    @Mock
    ReceivableRepository receivableRepository;
    @Mock
    PaymentService paymentService;
    @InjectMocks
    CreditService creditService;

    private final UUID hotelId = UUID.randomUUID();
    private CreditCustomer customer;

    @BeforeEach
    void setUp() {
        customer = CreditTestData.customer(hotelId, "100000");
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(Optional.of(customer));
    }

    @Test
    void repay_appliesToOldestFirst_andSpansTwoReceivables() {
        Receivable older = CreditTestData.receivable(customer, "10000", "0",
                EReceivableStatus.OUTSTANDING, LocalDate.now().plusDays(5), Instant.parse("2026-10-01T08:00:00Z"));
        Receivable newer = CreditTestData.receivable(customer, "8000", "0",
                EReceivableStatus.OUTSTANDING, LocalDate.now().plusDays(10), Instant.parse("2026-10-15T08:00:00Z"));

        when(receivableRepository.findByCustomerIdAndStatusInOrderByIssuedAtAsc(eq(customer.getId()), any()))
                .thenReturn(List.of(older, newer));

        // both receivables start unpaid, so "settled" means the applied slice equals the original
        when(paymentService.processPayment(any())).thenAnswer(inv -> {
            ProcessPaymentRequest req = inv.getArgument(0);
            boolean settled = req.amount().compareTo(req.amountOwed()) == 0;
            return new PaymentResult(settled, req.amountOwed().subtract(req.amount()), UUID.randomUUID());
        });

        RepayDebtResponse response = creditService.repay(customer.getId(),
                new RepayDebtRequest(new BigDecimal("12000"), EPaymentMethod.CASH, "REF-1"));

        ArgumentCaptor<ProcessPaymentRequest> captor = ArgumentCaptor.forClass(ProcessPaymentRequest.class);
        verify(paymentService, times(2)).processPayment(captor.capture());
        List<ProcessPaymentRequest> sent = captor.getAllValues();

        // oldest debt first, fully cleared
        Assertions.assertThat(sent.get(0).payableId()).isEqualTo(older.getId());
        Assertions.assertThat(sent.getFirst().payableType()).isEqualTo(EPayableType.RECEIVABLE);
        Assertions.assertThat(sent.get(0).amount()).isEqualByComparingTo("10000");
        Assertions.assertThat(sent.get(0).amountOwed()).isEqualByComparingTo("10000");

        // remainder goes to the next one
        Assertions.assertThat(sent.get(1).payableId()).isEqualTo(newer.getId());
        Assertions.assertThat(sent.get(1).amount()).isEqualByComparingTo("2000");

        Assertions.assertThat(older.getStatus()).isEqualTo(EReceivableStatus.SETTLED);
        Assertions.assertThat(older.getAmountPaid()).isEqualByComparingTo("10000");
        Assertions.assertThat(newer.getStatus()).isEqualTo(EReceivableStatus.PARTIALLY_SETTLED);
        Assertions.assertThat(newer.getAmountPaid()).isEqualByComparingTo("2000");

        Assertions.assertThat(response.totalApplied()).isEqualByComparingTo("12000");
        Assertions.assertThat(response.customerOutstandingBalance()).isEqualByComparingTo("6000");
        Assertions.assertThat(response.allocations()).hasSize(2);
    }

    @Test
    void repay_rejectsAmountAboveTotalOutstanding_andRecordsNoPayment() {
        Receivable r = CreditTestData.receivable(customer, "10000", "0",
                EReceivableStatus.OUTSTANDING, LocalDate.now().plusDays(5), Instant.now());
        when(receivableRepository.findByCustomerIdAndStatusInOrderByIssuedAtAsc(eq(customer.getId()), any()))
                .thenReturn(List.of(r));

        Assertions.assertThatThrownBy(() -> creditService.repay(customer.getId(),
                new RepayDebtRequest(new BigDecimal("10000.01"), EPaymentMethod.CASH, null)))
                .isInstanceOf(BusinessRuleViolationException.class);

        verifyNoInteractions(paymentService);
        Assertions.assertThat(r.getAmountPaid()).isEqualByComparingTo("0");
    }

    @Test
    void repay_rejectsWhenCustomerHasNoOutstandingDebt() {
        when(receivableRepository.findByCustomerIdAndStatusInOrderByIssuedAtAsc(eq(customer.getId()), any()))
                .thenReturn(List.of());

        Assertions.assertThatThrownBy(() -> creditService.repay(customer.getId(),
                new RepayDebtRequest(new BigDecimal("500"), EPaymentMethod.CASH, null)))
                .isInstanceOf(BusinessRuleViolationException.class);

        verifyNoInteractions(paymentService);
    }
}
