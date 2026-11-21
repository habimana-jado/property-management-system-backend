package rw.afriteck.pms.credit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.service.registry.ImportHttpServices;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.tenant.HotelTenantIdentifierResolver;
import rw.afriteck.pms.common.tenant.TenantContext;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.CreditCustomerRepository;
import rw.afriteck.pms.credit.repository.ReceivableRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(classes = CreditTenantIsolationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
class CreditTenantIsolationTest {

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

    private static final List<EReceivableStatus> ACTIVE =
            List.of(EReceivableStatus.OUTSTANDING, EReceivableStatus.PARTIALLY_SETTLED);

    @Autowired
    CreditCustomerRepository customerRepository;
    @Autowired
    ReceivableRepository receivableRepository;
    @Autowired
    TransactionTemplate tx;

    private final UUID hotelA = UUID.randomUUID();
    private final UUID hotelB = UUID.randomUUID();

    private <T> T asHotel(UUID hotelId, Supplier<T> work) {
        TenantContext.set(hotelId);
        try {
            return tx.execute(status -> work.get());
        } finally {
            TenantContext.clear();
        }
    }

    private CreditCustomer newCustomer(String phone) {
        CreditCustomer c = new CreditCustomer();
        c.setFullName("Test Customer");
        c.setPhoneNumber(phone);
        c.setCreditAllowed(true);
        c.setActive(true);
        c.setCreditLimit(new BigDecimal("100000"));
        return c;
    }

    private Receivable newReceivable(CreditCustomer customer, String amount) {
        Receivable r = new Receivable();
        r.setCustomer(customer);
        r.setSourceType(EPayableType.POS_SALE);
        r.setSourceId(UUID.randomUUID());
        r.setSourceReference("S-000001");
        r.setOriginalAmount(new BigDecimal(amount));
        r.setAmountPaid(BigDecimal.ZERO);
        r.setDueDate(LocalDate.now().plusDays(7));
        r.setIssuedAt(Instant.now());
        r.setStatus(EReceivableStatus.OUTSTANDING);
        r.setOriginBranchId(UUID.randomUUID());
        return r;
    }

    @Test
    void savedRowsCarryTheHotelFromTheContext() {
        CreditCustomer c = asHotel(hotelA, () -> customerRepository.save(newCustomer("+10000000001")));
        Receivable r = asHotel(hotelA, () -> receivableRepository.save(newReceivable(c, "1000")));

        assertThat(c.getHotelId()).isEqualTo(hotelA);
        assertThat(r.getHotelId()).isEqualTo(hotelA);
    }

    @Test
    void samePhoneIsAllowedInDifferentHotels_butNotWithinOne() {
        asHotel(hotelA, () -> customerRepository.saveAndFlush(newCustomer("+10000000002")));
        asHotel(hotelB, () -> customerRepository.saveAndFlush(newCustomer("+10000000002")));

        assertThatThrownBy(() -> asHotel(hotelA,
                () -> customerRepository.saveAndFlush(newCustomer("+10000000002"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void eachHotelOnlySeesItsOwnCustomers() {
        CreditCustomer ofB = asHotel(hotelB, () -> customerRepository.save(newCustomer("+10000000003")));

        assertThat(asHotel(hotelA, () -> customerRepository.findById(ofB.getId()))).isEmpty();
        assertThat(asHotel(hotelA, () -> customerRepository.findByPhoneNumber("+10000000003"))).isEmpty();
        assertThat(asHotel(hotelA, () -> customerRepository.existsByPhoneNumber("+10000000003"))).isFalse();
        assertThat(asHotel(hotelB, () -> customerRepository.findByPhoneNumber("+10000000003"))).isPresent();
    }

    @Test
    void theLockingLookupRespectsTheTenant() {
        CreditCustomer ofB = asHotel(hotelB, () -> customerRepository.save(newCustomer("+10000000004")));

        assertThat(asHotel(hotelA, () -> customerRepository.findByIdForUpdate(ofB.getId()))).isEmpty();
        assertThat(asHotel(hotelB, () -> customerRepository.findByIdForUpdate(ofB.getId()))).isPresent();
    }

    @Test
    void sumsAndFetchJoinsIgnoreOtherHotels() {
        CreditCustomer ofB = asHotel(hotelB, () -> customerRepository.save(newCustomer("+10000000005")));
        asHotel(hotelB, () -> receivableRepository.save(newReceivable(ofB, "4000")));

        assertThat(asHotel(hotelA, () -> receivableRepository.sumOutstandingByCustomer(ofB.getId(), ACTIVE)))
                .isEqualByComparingTo("0");
        assertThat(asHotel(hotelA, () -> receivableRepository.findActiveWithCustomer(ACTIVE))).isEmpty();

        List<Receivable> seenByB = asHotel(hotelB, () -> receivableRepository.findActiveWithCustomer(ACTIVE));
        assertThat(seenByB).hasSize(1);
        assertThat(seenByB.get(0).getCustomer().getFullName()).isEqualTo("Test Customer");
    }
}
