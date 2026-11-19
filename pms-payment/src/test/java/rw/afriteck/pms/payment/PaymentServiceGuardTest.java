package rw.afriteck.pms.payment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.tenant.TenantContext;
import rw.afriteck.pms.payment.dtos.ProcessPaymentRequest;
import rw.afriteck.pms.payment.mapper.PaymentMapper;
import rw.afriteck.pms.payment.repository.PaymentRepo;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PaymentServiceGuardTest {

    @Mock
    PaymentRepo paymentRepo;
    @Mock
    PaymentMapper paymentMapper;
    @InjectMocks
    rw.afriteck.pms.payment.service.impl.PaymentService service;

    @AfterEach
    void cleanUp() { TenantContext.clear(); }

    private ProcessPaymentRequest request(String amount) {
        return new ProcessPaymentRequest(UUID.randomUUID(), EPayableType.POS_SALE,
                new BigDecimal("10000"), amount == null ? null : new BigDecimal(amount),
                EPaymentMethod.CASH, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-500"})
    void rejectsZeroAndNegativeAmounts(String amount) {
        TenantContext.set(UUID.randomUUID());

        assertThatThrownBy(() -> service.processPayment(request(amount)))
                .isInstanceOf(BusinessRuleViolationException.class);

        verifyNoInteractions(paymentRepo);
    }

    @Test
    void rejectsANullAmount() {
        TenantContext.set(UUID.randomUUID());

        assertThatThrownBy(() -> service.processPayment(request(null)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void refusesToRunWithoutATenant() {
        assertThatThrownBy(() -> service.processPayment(request("100")))
                .isInstanceOf(ForbiddenException.class);

        verifyNoInteractions(paymentRepo);
    }
}
