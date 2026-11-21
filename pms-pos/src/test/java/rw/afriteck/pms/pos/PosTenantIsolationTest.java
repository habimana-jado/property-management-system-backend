package rw.afriteck.pms.pos;

import org.junit.jupiter.api.Test;
import org.mockito.internal.util.Supplier;
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
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.tenant.HotelTenantIdentifierResolver;
import rw.afriteck.pms.common.tenant.TenantContext;
import rw.afriteck.pms.pos.model.*;
import rw.afriteck.pms.pos.repository.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = PosTenantIsolationTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
class PosTenantIsolationTest {

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
    POSTerminalRepository terminalRepo;
    @Autowired
    POSProductCategoryRepository categoryRepo;
    @Autowired
    POSProductRepository productRepo;
    @Autowired
    POSRegisterSessionRepository sessionRepo;
    @Autowired
    POSSaleRepository saleRepo;
    @Autowired POSSaleItemRepository saleItemRepo;
    @Autowired POSSaleNumberCounterRepository counterRepo;
    @Autowired
    TransactionTemplate tx;

    private final UUID hotelA = UUID.randomUUID();
    private final UUID hotelB = UUID.randomUUID();
    private final UUID branchOfA = UUID.randomUUID();
    private final UUID branchOfB = UUID.randomUUID();

    private <T> T asHotel(UUID hotelId, Supplier<T> work) {
        TenantContext.set(hotelId);
        try {
            return tx.execute(status -> work.get());
        } finally {
            TenantContext.clear();
        }
    }

    private record Chain(POSTerminal terminal, POSProductCategory category, POSProduct product,
                         POSRegisterSession session, POSSale sale, POSSaleItem item,
                         POSSaleNumberCounter counter) {}

    /** Builds a whole terminal -> session -> sale -> item chain; call it inside asHotel. */
    private Chain buildChain(UUID branchId) {
        POSTerminal terminal = new POSTerminal();
        terminal.setHotelBranchId(branchId);
        terminal.setCode("TILL-1");
        terminal.setName("Till 1");
        terminal = terminalRepo.save(terminal);

        POSProductCategory category = new POSProductCategory();
        category.setHotelBranchId(branchId);
        category.setCategoryName("Drinks");
        category = categoryRepo.save(category);

        POSProduct product = new POSProduct();
        product.setHotelBranchId(branchId);
        product.setCategory(category);
        product.setSku("SKU-1");
        product.setName("Water");
        product.setUnitPrice(new BigDecimal("500"));
        product.setTrackInventory(false);
        product = productRepo.save(product);

        POSRegisterSession session = new POSRegisterSession();
        session.setHotelBranchId(branchId);
        session.setTerminal(terminal);
        session.setCashierId(UUID.randomUUID());
        session.setOpeningCash(BigDecimal.ZERO);
        session = sessionRepo.save(session);

        POSSale sale = new POSSale();
        sale.setHotelBranchId(branchId);
        sale.setRegisterSession(session);
        sale.setSaleNumber("S-000001");
        sale.setCashierId(session.getCashierId());
        sale = saleRepo.save(sale);

        POSSaleItem item = new POSSaleItem();
        item.setSale(sale);
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPriceAtSaleTime(new BigDecimal("500"));
        item.setLineTotal(new BigDecimal("500"));
        item = saleItemRepo.save(item);

        POSSaleNumberCounter counter = new POSSaleNumberCounter();
        counter.setHotelBranchId(branchId);   // assigned id: save() goes through merge
        counter = counterRepo.save(counter);

        return new Chain(terminal, category, product, session, sale, item, counter);
    }

    @Test
    void everyRowInTheChainCarriesTheHotelFromTheContext() {
        Chain chain = asHotel(hotelA, () -> buildChain(branchOfA));

        assertThat(chain.terminal().getHotelId()).isEqualTo(hotelA);
        assertThat(chain.category().getHotelId()).isEqualTo(hotelA);
        assertThat(chain.product().getHotelId()).isEqualTo(hotelA);
        assertThat(chain.session().getHotelId()).isEqualTo(hotelA);
        assertThat(chain.sale().getHotelId()).isEqualTo(hotelA);
        assertThat(chain.item().getHotelId()).isEqualTo(hotelA);
        assertThat(chain.counter().getHotelId()).isEqualTo(hotelA);
    }

    @Test
    void anotherHotelSeesNothingOfTheChain_evenWhenItNamesTheRealBranchId() {
        Chain ofB = asHotel(hotelB, () -> buildChain(branchOfB));

        // sanity: the owner sees everything, so the "empty" results below mean something
        assertThat(asHotel(hotelB, () -> terminalRepo.findByHotelBranchId(branchOfB))).hasSize(1);
        assertThat(asHotel(hotelB, () -> saleItemRepo.findBySaleId(ofB.sale().getId()))).hasSize(1);
        assertThat(asHotel(hotelB, () -> counterRepo.findForUpdate(branchOfB))).isPresent();

        assertThat(asHotel(hotelA, () -> terminalRepo.findById(ofB.terminal().getId()))).isEmpty();
        assertThat(asHotel(hotelA, () -> terminalRepo.findByHotelBranchId(branchOfB))).isEmpty();
        assertThat(asHotel(hotelA, () -> terminalRepo.existsByHotelBranchIdAndCode(branchOfB, "TILL-1"))).isFalse();
        assertThat(asHotel(hotelA, () -> productRepo.findByHotelBranchIdAndStatus(branchOfB, ERecordStatus.ACTIVE))).isEmpty();
        assertThat(asHotel(hotelA, () -> saleRepo.findById(ofB.sale().getId()))).isEmpty();
        assertThat(asHotel(hotelA, () -> saleRepo.findByIdAndHotelBranchId(ofB.sale().getId(), branchOfB))).isEmpty();
        assertThat(asHotel(hotelA, () -> saleRepo.findIdsByRegisterSessionId(ofB.session().getId()))).isEmpty();
        assertThat(asHotel(hotelA, () -> saleItemRepo.findBySaleId(ofB.sale().getId()))).isEmpty();
        assertThat(asHotel(hotelA, () -> counterRepo.findForUpdate(branchOfB))).isEmpty();
    }
}