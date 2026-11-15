package rw.afriteck.pms.stock.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.stock.api.command.ConsumeForSaleCommand;
import rw.afriteck.pms.stock.api.command.SaleLine;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.enums.EItemType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.api.query.MovementFilter;
import rw.afriteck.pms.stock.api.setup.ItemCommand;
import rw.afriteck.pms.stock.api.setup.ItemView;

/** Several rounds each, because a race only shows up when the threads actually interleave. */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "PMS_IT_DB_URL", matches = ".+")
class StockConcurrencyTest extends AbstractStockIntegrationTest {

    private static final int ROUNDS = 5;

    @Test
    void only_one_of_two_simultaneous_sales_gets_the_last_unit() throws Exception {
        World w = newWorld();

        for (int round = 1; round <= ROUNDS; round++) {
            openingBalance(w.bar().id(), line(w.soda(), "1", w.unit("PCS")));      // exactly one unit on the shelf

            List<Throwable> results = runConcurrently(List.of(sellOneSoda(w), sellOneSoda(w)));

            assertThat(successes(results)).as("round %d: exactly one sale wins", round).isEqualTo(1);
            assertThat(results).filteredOn(t -> t != null).singleElement().isInstanceOf(InsufficientStockException.class);
            assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("0");
        }

        assertThat(query.movements(new MovementFilter(w.bar().id(), w.soda().id(), Set.of(EMovementType.SALE),
                null, null, null, null, null), 0, 50).totalElements()).isEqualTo(ROUNDS);
        assertLedgerConsistent(w.branchId(), false);
    }

    @Test
    void two_first_ever_receipts_for_the_same_item_both_succeed_and_add_up() throws Exception {
        World w = newWorld();

        for (int round = 1; round <= ROUNDS; round++) {
            // a brand-new item has no balance row yet: both threads race to create it
            ItemView fresh = catalog.createItem(new ItemCommand("FR" + w.tag() + round, "Fresh " + round, null,
                    w.category().id(), w.unit("PCS").id(), EItemType.CONSUMABLE, null));

            List<Throwable> results = runConcurrently(List.of(
                    () -> receipt(w.main().id(), line(fresh, "10", w.unit("PCS"))),
                    () -> receipt(w.main().id(), line(fresh, "10", w.unit("PCS")))));

            assertThat(results).as("round %d: no receipt may fail", round).containsOnlyNulls();
            assertThat(onHand(w.main().id()).get(fresh.code())).isEqualByComparingTo("20");
            assertThat(query.movements(new MovementFilter(w.main().id(), fresh.id(), null, null, null, null, null,
                    null), 0, 50).items()).extracting(m -> m.balanceAfter().intValue()).containsExactlyInAnyOrder(10, 20);
        }
        assertLedgerConsistent(w.branchId(), false);
    }

    @Test
    void opposing_transfers_on_the_same_item_do_not_deadlock() throws Exception {
        World w = newWorld();
        openingBalance(w.main().id(), line(w.soda(), "50", w.unit("PCS")));
        openingBalance(w.bar().id(), line(w.soda(), "50", w.unit("PCS")));

        for (int round = 1; round <= ROUNDS; round++) {
            List<Throwable> results = runConcurrently(List.of(
                    () -> operations.transfer(new TransferCommand(w.main().id(), w.bar().id(),
                            ETransferPurpose.DISTRIBUTION, "down", List.of(line(w.soda(), "1", w.unit("PCS"))), STAFF)),
                    () -> operations.transfer(new TransferCommand(w.bar().id(), w.main().id(),
                            ETransferPurpose.RETURN, "up", List.of(line(w.soda(), "1", w.unit("PCS"))), STAFF))));

            assertThat(results).as("round %d: both transfers complete", round).containsOnlyNulls();
        }

        assertThat(onHand(w.main().id()).get(w.soda().code()).add(onHand(w.bar().id()).get(w.soda().code())))
                .isEqualByComparingTo("100");                                          // nothing created or lost
        assertLedgerConsistent(w.branchId(), false);
    }

    private Callable<Object> sellOneSoda(World w) {
        return () -> stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, w.terminalId(), null, EMovementSourceType.POS_SALE_LINE, UUID.randomUUID(),
                List.of(new SaleLine(UUID.randomUUID(), w.soda().id(), null, bd("1"), true)), STAFF));
    }
}