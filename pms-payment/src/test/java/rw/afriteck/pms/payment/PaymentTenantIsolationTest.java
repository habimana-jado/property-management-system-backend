package rw.afriteck.pms.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.tenant.HotelTenantIdentifierResolver;
import rw.afriteck.pms.common.tenant.TenantContext;
import rw.afriteck.pms.payment.enums.EPaymentStatus;
import rw.afriteck.pms.payment.model.Payment;
import rw.afriteck.pms.payment.repository.PaymentRepo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = PaymentTenantIsolationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
class PaymentTenantIsolationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {}

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.tenant_identifier_resolver",
                HotelTenantIdentifierResolver.class::getName);
    }

    @Autowired
    PaymentRepo paymentRepo;
    @Autowired
    TransactionTemplate tx;

    // JUnit creates a fresh instance per test, so every test gets new hotels
    private final UUID hotelA = UUID.randomUUID();
    private final UUID hotelB = UUID.randomUUID();

    /** Each call is its own transaction and session, so the tenant is read fresh every time. */
    private <T> T asHotel(UUID hotelId, Supplier<T> work) {
        TenantContext.set(hotelId);
        try {
            return tx.execute(status -> work.get());
        } finally {
            TenantContext.clear();
        }
    }

    // the only place to adapt if your Payment fields differ
    private Payment newPayment(UUID payableId, String amount) {
        Payment p = new Payment();
        p.setPayableId(payableId);
        p.setPayableType(EPayableType.POS_SALE);
        p.setAmount(new BigDecimal(amount));
        p.setMethod(EPaymentMethod.CASH);
        p.setStatus(EPaymentStatus.COMPLETED);
        p.setPaidAt(Instant.now());
        return p;
    }

    @Test
    void aSavedPaymentCarriesTheHotelFromTheContext() {
        Payment saved = asHotel(hotelA, () -> paymentRepo.save(newPayment(UUID.randomUUID(), "1000")));

        assertThat(saved.getHotelId()).isEqualTo(hotelA);
    }

    @Test
    void eachHotelOnlySeesItsOwnPayments() {
        asHotel(hotelA, () -> paymentRepo.saveAll(List.of(
                newPayment(UUID.randomUUID(), "1000"), newPayment(UUID.randomUUID(), "2000"))));
        asHotel(hotelB, () -> paymentRepo.save(newPayment(UUID.randomUUID(), "3000")));

        List<Payment> seenByA = asHotel(hotelA, paymentRepo::findAll);
        List<Payment> seenByB = asHotel(hotelB, paymentRepo::findAll);

        assertThat(seenByA).hasSize(2).allMatch(p -> p.getHotelId().equals(hotelA));
        assertThat(seenByB).hasSize(1).allMatch(p -> p.getHotelId().equals(hotelB));
    }

    @Test
    void aPaymentFromAnotherHotelCannotBeLoadedById() {
        Payment ofB = asHotel(hotelB, () -> paymentRepo.save(newPayment(UUID.randomUUID(), "3000")));

        assertThat(asHotel(hotelA, () -> paymentRepo.findById(ofB.getId()))).isEmpty();
        assertThat(asHotel(hotelA, () -> paymentRepo.existsById(ofB.getId()))).isFalse();
        assertThat(asHotel(hotelB, () -> paymentRepo.findById(ofB.getId()))).isPresent();
    }

    @Test
    void aggregateQueriesIgnoreOtherHotels() {
        UUID saleId = UUID.randomUUID();
        asHotel(hotelB, () -> paymentRepo.save(newPayment(saleId, "5000")));

        BigDecimal seenByA = asHotel(hotelA, () -> paymentRepo.sumAmountByPayableIdsAndTypeAndMethod(
                List.of(saleId), EPayableType.POS_SALE, EPaymentMethod.CASH));
        BigDecimal seenByB = asHotel(hotelB, () -> paymentRepo.sumAmountByPayableIdsAndTypeAndMethod(
                List.of(saleId), EPayableType.POS_SALE, EPaymentMethod.CASH));

        assertThat(seenByA).isEqualByComparingTo("0");
        assertThat(seenByB).isEqualByComparingTo("5000");
    }

    @Test
    void withoutATenantNothingIsVisible() {
        asHotel(hotelA, () -> paymentRepo.save(newPayment(UUID.randomUUID(), "1000")));

        List<Payment> visible = tx.execute(status -> paymentRepo.findAll()); // no tenant set

        assertThat(visible).isEmpty();
    }
}
