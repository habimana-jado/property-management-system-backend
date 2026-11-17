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
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.credit.dtos.GrantCreditRequest;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.CreditCustomerRepository;
import rw.afriteck.pms.credit.repository.ReceivableRepository;
import rw.afriteck.pms.credit.service.CreditService;
import rw.afriteck.pms.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceGrantTest {

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
        customer = CreditTestData.customer(hotelId, "50000");
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(Optional.of(customer));
    }

    // single place to adapt if GrantCreditRequest's component order differs
    private GrantCreditRequest request(String amount, LocalDate dueDate) {
        return new GrantCreditRequest(hotelId, customer.getId(), EPayableType.TABLE_BILL,
                UUID.randomUUID(), "MNB-000001", new BigDecimal(amount), dueDate);
    }

    @Test
    void grantCredit_savesReceivable_whenWithinLimit() {
        when(receivableRepository.existsBySourceTypeAndSourceId(any(), any())).thenReturn(false);
        when(receivableRepository.sumOutstandingByCustomer(eq(customer.getId()), any()))
                .thenReturn(new BigDecimal("20000"));

        creditService.grantCredit(request("15000", LocalDate.now().plusDays(7)));

        ArgumentCaptor<Receivable> captor = ArgumentCaptor.forClass(Receivable.class);
        verify(receivableRepository).save(captor.capture());
        Receivable saved = captor.getValue();

        Assertions.assertThat(saved.getCustomer()).isSameAs(customer);
        Assertions.assertThat(saved.getOriginalAmount()).isEqualByComparingTo("15000");
        Assertions.assertThat(saved.getAmountPaid()).isEqualByComparingTo("0");
        Assertions.assertThat(saved.getStatus()).isEqualTo(EReceivableStatus.OUTSTANDING);
    }

    @Test
    void grantCredit_allowsAmountThatExactlyReachesTheLimit() {
        when(receivableRepository.existsBySourceTypeAndSourceId(any(), any())).thenReturn(false);
        when(receivableRepository.sumOutstandingByCustomer(eq(customer.getId()), any()))
                .thenReturn(new BigDecimal("40000"));

        creditService.grantCredit(request("10000", LocalDate.now().plusDays(7))); // 40000 + 10000 == 50000

        verify(receivableRepository).save(any(Receivable.class));
    }

    @Test
    void grantCredit_rejectsWhenLimitWouldBeExceeded() {
        when(receivableRepository.existsBySourceTypeAndSourceId(any(), any())).thenReturn(false);
        when(receivableRepository.sumOutstandingByCustomer(eq(customer.getId()), any()))
                .thenReturn(new BigDecimal("40000"));

        Assertions.assertThatThrownBy(() -> creditService.grantCredit(request("10000.01", LocalDate.now().plusDays(7))))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(receivableRepository, never()).save(any());
    }

    @Test
    void grantCredit_rejectsDuplicateGrantForSameSource() {
        when(receivableRepository.existsBySourceTypeAndSourceId(any(), any())).thenReturn(true);

        Assertions.assertThatThrownBy(() -> creditService.grantCredit(request("1000", LocalDate.now().plusDays(7))))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(receivableRepository, never()).save(any());
    }

    @Test
    void grantCredit_rejectsCustomerWithoutCreditPermission() {
        customer.setCreditAllowed(false);

        Assertions.assertThatThrownBy(() -> creditService.grantCredit(request("1000", LocalDate.now().plusDays(7))))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(receivableRepository, never()).save(any());
    }

    @Test
    void grantCredit_rejectsPastDueDate() {
        Assertions.assertThatThrownBy(() -> creditService.grantCredit(request("1000", LocalDate.now().minusDays(1))))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(receivableRepository, never()).save(any());
    }

    @Test
    void grantCredit_rejectsCustomerFromAnotherHotel() {
        GrantCreditRequest otherHotel = new GrantCreditRequest(UUID.randomUUID(), customer.getId(),
                EPayableType.TABLE_BILL, UUID.randomUUID(), "MNB-000002",
                new BigDecimal("1000"), LocalDate.now().plusDays(7));

        Assertions.assertThatThrownBy(() -> creditService.grantCredit(otherHotel))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(receivableRepository, never()).save(any());
    }
}
