package rw.afriteck.pms;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/**
 * Minimal application for the stock integration tests (test sources only, never shipped).
 *
 * Lives in rw.afriteck.pms so that entity and repository scanning cover every module on the classpath
 * (stock, property, common), while component scanning is limited to the stock module.
 * Auditing is on with a fixed auditor so the Auditable columns can be asserted.
 */
@SpringBootApplication(scanBasePackages = "rw.afriteck.pms.stock")
@EnableJpaAuditing(auditorAwareRef = "testAuditor")
public class StockTestApplication {

    public static final UUID TEST_USER = UUID.fromString("00000000-0000-0000-0000-0000000000b1");

    @Bean
    AuditorAware<UUID> testAuditor() {
        return () -> Optional.of(TEST_USER);
    }
}