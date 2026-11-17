package rw.afriteck.pms.credit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.credit.dtos.WriteOffRequest;
import rw.afriteck.pms.credit.dtos.WriteOffResponse;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.CreditCustomerRepository;
import rw.afriteck.pms.credit.repository.ReceivableRepository;
import rw.afriteck.pms.credit.service.CreditService;
import rw.afriteck.pms.payment.service.PaymentService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceWriteOffTest {

    @Mock
    CreditCustomerRepository customerRepository;
    @Mock
    ReceivableRepository receivableRepository;
    @Mock
    PaymentService paymentService;
    @InjectMocks
    CreditService creditService;

    private CreditCustomer customer;

    @BeforeEach
    void setUp() {
        customer = CreditTestData.customer(UUID.randomUUID(), "100000");
    }

    private void stubLookups(Receivable r) {
        when(receivableRepository.findCustomerIdById(r.getId())).thenReturn(Optional.of(customer.getId()));
        when(customerRepository.findByIdForUpdate(customer.getId())).thenReturn(Optional.of(customer));
        when(receivableRepository.findById(r.getId())).thenReturn(Optional.of(r));
    }

    @Test
    void writeOff_recordsOutstandingAmount_closesDebt_andBlocksFurtherCredit() {
        Receivable r = CreditTestData.receivable(customer, "10000", "4000",
                EReceivableStatus.PARTIALLY_SETTLED, LocalDate.now().minusDays(40), Instant.now());
        stubLookups(r);

        WriteOffResponse response = creditService.writeOff(r.getId(),
                new WriteOffRequest("Customer unreachable", "Manager A"));

        assertThat(response.writtenOffAmount()).isEqualByComparingTo("6000"); // 10000 - 4000, not the original
        assertThat(r.getStatus()).isEqualTo(EReceivableStatus.WRITTEN_OFF);
        assertThat(r.getWrittenOffAmount()).isEqualByComparingTo("6000");
        assertThat(r.getWriteOffReason()).isEqualTo("Customer unreachable");
        assertThat(r.getWrittenOffBy()).isEqualTo("Manager A");
        assertThat(r.getWrittenOffAt()).isNotNull();
        assertThat(r.outstandingBalance()).isEqualByComparingTo("0");
        assertThat(customer.isCreditAllowed()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = EReceivableStatus.class, names = {"SETTLED", "WRITTEN_OFF"})
    void writeOff_rejectsDebtsThatAreNotActive(EReceivableStatus status) {
        Receivable r = CreditTestData.receivable(customer, "10000", "10000",
                status, LocalDate.now().minusDays(10), Instant.now());
        stubLookups(r);

        assertThatThrownBy(() -> creditService.writeOff(r.getId(), new WriteOffRequest("x", "y")))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(r.getStatus()).isEqualTo(status);   // untouched
        assertThat(customer.isCreditAllowed()).isTrue(); // customer untouched
    }

    @Test
    void writeOff_locksCustomerBeforeLoadingReceivable() {
        Receivable r = CreditTestData.receivable(customer, "5000", "0",
                EReceivableStatus.OUTSTANDING, LocalDate.now(), Instant.now());
        stubLookups(r);

        creditService.writeOff(r.getId(), new WriteOffRequest("x", "y"));

        InOrder order = inOrder(customerRepository, receivableRepository);
        order.verify(customerRepository).findByIdForUpdate(customer.getId());
        order.verify(receivableRepository).findById(r.getId());
    }

    @Test
    void writeOff_throwsWhenReceivableDoesNotExist() {
        UUID missing = UUID.randomUUID();
        when(receivableRepository.findCustomerIdById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.writeOff(missing, new WriteOffRequest("x", "y")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
