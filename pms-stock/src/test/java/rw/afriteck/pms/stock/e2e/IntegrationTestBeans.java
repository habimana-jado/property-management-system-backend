package rw.afriteck.pms.stock.e2e;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import rw.afriteck.pms.stock.api.OutletDirectory;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Stands in for the outlet modules: tests register outlets (terminals, restaurants) and the branch they belong to. */
@TestConfiguration
public class IntegrationTestBeans {

    public static class TestOutlets {
        private final Map<UUID, UUID> branchByOutlet = new ConcurrentHashMap<>();

        public UUID register(UUID outletId, UUID branchId) {
            branchByOutlet.put(outletId, branchId);
            return outletId;
        }

        Optional<UUID> branchOf(UUID outletId) {
            return Optional.ofNullable(branchByOutlet.get(outletId));
        }
    }

    @Bean
    TestOutlets testOutlets() {
        return new TestOutlets();
    }

    @Bean
    OutletDirectory testPosTerminals(TestOutlets outlets) {
        return directory(EOutletType.POS_TERMINAL, outlets);
    }

    @Bean
    OutletDirectory testRestaurants(TestOutlets outlets) {
        return directory(EOutletType.RESTAURANT, outlets);
    }

    private static OutletDirectory directory(EOutletType type, TestOutlets outlets) {
        return new OutletDirectory() {
            @Override public EOutletType type() { return type; }
            @Override public Optional<UUID> branchIdOf(UUID outletId) { return outlets.branchOf(outletId); }
        };
    }
}