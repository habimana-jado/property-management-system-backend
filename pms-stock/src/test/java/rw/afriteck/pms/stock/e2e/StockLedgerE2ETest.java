package rw.afriteck.pms.stock.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.support.TransactionTemplate;
import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.common.exception.StockNotFoundException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.command.ConsumeForSaleCommand;
import rw.afriteck.pms.stock.api.command.ReverseConsumptionCommand;
import rw.afriteck.pms.stock.api.command.SaleLine;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.api.query.StockOnHandFilter;
import rw.afriteck.pms.stock.api.query.MovementFilter;
import rw.afriteck.pms.stock.api.view.MovementRow;
import rw.afriteck.pms.stock.api.view.StockDocumentView;

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "PMS_IT_DB_URL", matches = ".+")
class StockLedgerE2ETest extends AbstractStockIntegrationTest {

    // ------------------------------------------------------------------ receipts and transfers

    @Test
    void receipt_into_main_converts_units_numbers_the_document_and_writes_the_ledger() {
        World w = newWorld();

        UUID doc = receipt(w.main().id(),
                line(w.beef(), "5", w.unit("KG")),             // 5 kg  -> 5000 g
                line(w.soda(), "2", w.unit("CARTON")),         // 2 cartons of 24 -> 48 pcs
                line(w.bun(), "100", w.unit("PCS")));

        Map<String, BigDecimal> main = onHand(w.main().id());
        assertThat(main.get(w.beef().code())).isEqualByComparingTo("5000");
        assertThat(main.get(w.soda().code())).isEqualByComparingTo("48");
        assertThat(main.get(w.bun().code())).isEqualByComparingTo("100");

        StockDocumentView view = query.document(doc);
        assertThat(view.documentType()).isEqualTo(EDocumentType.RECEIPT);
        assertThat(view.documentNo()).endsWith("-RCV-000001");
        assertThat(view.postedBy()).isEqualTo(STAFF);
        assertThat(view.lines()).hasSize(3);

        List<MovementRow> movements = query.movements(
                new MovementFilter(null, null, null, null, null, doc, null, null), 0, 50).items();
        assertThat(movements).hasSize(3).allSatisfy(m -> {
            assertThat(m.movementType()).isEqualTo(EMovementType.RECEIPT);
            assertThat(m.performedBy()).isEqualTo(STAFF);
            assertThat(m.balanceAfter()).isEqualByComparingTo(m.quantity());     // first movement of each item
        });

        // a second receipt gets the next number
        UUID second = receipt(w.main().id(), line(w.bun(), "1", w.unit("PCS")));
        assertThat(query.document(second).documentNo()).endsWith("-RCV-000002");

        assertLedgerConsistent(w.branchId(), false);
    }

    @Test
    void transfer_moves_stock_between_stores_in_one_document_with_paired_movements() {
        World w = newWorld();
        receipt(w.main().id(), line(w.beef(), "5", w.unit("KG")), line(w.soda(), "48", w.unit("PCS")));

        UUID doc = operations.transfer(new TransferCommand(w.main().id(), w.bar().id(), ETransferPurpose.DISTRIBUTION,
                "to bar", List.of(line(w.beef(), "2", w.unit("KG")), line(w.soda(), "24", w.unit("PCS"))), STAFF));

        assertThat(onHand(w.main().id()).get(w.beef().code())).isEqualByComparingTo("3000");
        assertThat(onHand(w.bar().id()).get(w.beef().code())).isEqualByComparingTo("2000");
        assertThat(onHand(w.main().id()).get(w.soda().code())).isEqualByComparingTo("24");
        assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("24");

        assertThat(query.document(doc).documentNo()).endsWith("-TRF-000001");
        List<MovementRow> movements = query.movements(
                new MovementFilter(null, null, null, null, null, doc, null, null), 0, 50).items();
        assertThat(movements).hasSize(4);
        assertThat(movements).filteredOn(m -> m.movementType() == EMovementType.TRANSFER_OUT)
                .extracting(MovementRow::storeId).containsOnly(w.main().id());
        assertThat(movements).filteredOn(m -> m.movementType() == EMovementType.TRANSFER_IN)
                .extracting(MovementRow::storeId).containsOnly(w.bar().id());

        assertLedgerConsistent(w.branchId(), false);
    }

    // ------------------------------------------------------------------ sales

    @Test
    void pos_sale_deducts_items_and_recipe_ingredients_from_the_assigned_store() {
        World w = newWorld();
        stockUp(w);
        UUID saleId = UUID.randomUUID();
        UUID soda3 = UUID.randomUUID(), burgers2 = UUID.randomUUID(), untracked = UUID.randomUUID();

        List<UUID> movementIds = stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, w.terminalId(), null, EMovementSourceType.POS_SALE_LINE, saleId,
                List.of(new SaleLine(soda3, w.soda().id(), null, bd("3"), true),
                        new SaleLine(burgers2, null, w.burger().id(), bd("2"), true),
                        new SaleLine(untracked, w.soda().id(), null, bd("5"), false)),   // not tracked: ignored
                STAFF));

        assertThat(movementIds).hasSize(3);                                 // soda + beef + bun
        Map<String, BigDecimal> bar = onHand(w.bar().id());
        assertThat(bar.get(w.soda().code())).isEqualByComparingTo("21");    // 24 - 3
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("1700"); // 2000 - 2 * 150
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("38");    // 40 - 2

        List<MovementRow> movements = query.movements(new MovementFilter(
                null, null, null, null, null, null, EMovementSourceType.POS_SALE_LINE, saleId), 0, 50).items();
        assertThat(movements).hasSize(3).allSatisfy(m -> {
            assertThat(m.movementType()).isEqualTo(EMovementType.SALE);
            assertThat(m.quantity().signum()).isNegative();
            assertThat(m.performedBy()).isEqualTo(STAFF);
        });
        assertThat(movements).filteredOn(m -> m.itemCode().equals(w.soda().code()))
                .extracting(MovementRow::sourceLineId).containsExactly(soda3);
        assertThat(movements).filteredOn(m -> !m.itemCode().equals(w.soda().code()))
                .extracting(MovementRow::sourceLineId).containsOnly(burgers2);

        assertLedgerConsistent(w.branchId(), false);
    }

    @Test
    void restaurant_order_resolves_the_restaurants_store_and_records_table_bill_lines() {
        World w = newWorld();
        stockUp(w);
        UUID billId = UUID.randomUUID(), lineId = UUID.randomUUID();

        stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.RESTAURANT, w.restaurantId(), null, EMovementSourceType.TABLE_BILL_LINE, billId,
                List.of(new SaleLine(lineId, null, w.burger().id(), bd("1"), true)), STAFF));

        Map<String, BigDecimal> bar = onHand(w.bar().id());
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("1850");
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("39");
        assertThat(query.movements(new MovementFilter(null, null, null, null, null, null,
                EMovementSourceType.TABLE_BILL_LINE, billId), 0, 50).items()).hasSize(2);
        assertLedgerConsistent(w.branchId(), false);
    }

    @Test
    void an_outlet_without_an_assigned_store_cannot_sell() {
        World w = newWorld();
        stockUp(w);
        UUID unassigned = outlets.register(UUID.randomUUID(), w.branchId());

        assertThatThrownBy(() -> stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, unassigned, null, EMovementSourceType.POS_SALE_LINE, UUID.randomUUID(),
                List.of(new SaleLine(UUID.randomUUID(), w.soda().id(), null, bd("1"), true)), STAFF)))
                .isInstanceOf(StockRuleViolationException.class)
                .hasMessageContaining("no default store");
    }

    @Test
    void oversell_is_rejected_and_leaves_balances_and_movements_untouched() {
        World w = newWorld();
        stockUp(w);
        Map<String, BigDecimal> before = onHand(w.bar().id());

        assertThatThrownBy(() -> stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, w.terminalId(), null, EMovementSourceType.POS_SALE_LINE, UUID.randomUUID(),
                List.of(new SaleLine(UUID.randomUUID(), w.bun().id(), null, bd("1"), true),     // fine on its own
                        new SaleLine(UUID.randomUUID(), w.soda().id(), null, bd("100"), true)), // 24 on hand
                STAFF)))
                .isInstanceOf(InsufficientStockException.class);

        Map<String, BigDecimal> after = onHand(w.bar().id());
        assertThat(after.get(w.soda().code())).isEqualByComparingTo(before.get(w.soda().code()));
        assertThat(after.get(w.bun().code())).isEqualByComparingTo(before.get(w.bun().code()));  // rolled back too
        assertThat(query.movements(new MovementFilter(w.bar().id(), w.bun().id(), Set.of(EMovementType.SALE),
                null, null, null, null, null), 0, 50).totalElements()).isZero();
        assertLedgerConsistent(w.branchId(), false);
    }

    @Test
    void branch_setting_can_allow_negative_stock_for_sales() {
        World w = newWorld();
        stockUp(w);

        structure.updateBranchSettings(true);
        stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, w.terminalId(), null, EMovementSourceType.POS_SALE_LINE, UUID.randomUUID(),
                List.of(new SaleLine(UUID.randomUUID(), w.soda().id(), null, bd("30"), true)), STAFF));
        assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("-6");
        assertLedgerConsistent(w.branchId(), true);

        structure.updateBranchSettings(false);
        assertThatThrownBy(() -> stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, w.terminalId(), null, EMovementSourceType.POS_SALE_LINE, UUID.randomUUID(),
                List.of(new SaleLine(UUID.randomUUID(), w.soda().id(), null, bd("1"), true)), STAFF)))
                .isInstanceOf(InsufficientStockException.class);
    }

    // ------------------------------------------------------------------ reversal

    @Test
    void reversal_restores_stock_links_the_rows_and_cannot_be_applied_twice() {
        World w = newWorld();
        stockUp(w);
        UUID saleId = UUID.randomUUID();
        stockApi.consumeForSale(new ConsumeForSaleCommand(
                EOutletType.POS_TERMINAL, w.terminalId(), null, EMovementSourceType.POS_SALE_LINE, saleId,
                List.of(new SaleLine(UUID.randomUUID(), w.soda().id(), null, bd("3"), true),
                        new SaleLine(UUID.randomUUID(), null, w.burger().id(), bd("2"), true)), STAFF));

        int reversed = stockApi.reverseConsumption(new ReverseConsumptionCommand(
                EMovementSourceType.POS_SALE_LINE, saleId, null, "customer return", STAFF));

        assertThat(reversed).isEqualTo(3);
        Map<String, BigDecimal> bar = onHand(w.bar().id());
        assertThat(bar.get(w.soda().code())).isEqualByComparingTo("24");
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("2000");
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("40");

        List<MovementRow> rows = query.movements(new MovementFilter(
                null, null, null, null, null, null, EMovementSourceType.POS_SALE_LINE, saleId), 0, 50).items();
        assertThat(rows).hasSize(6);
        assertThat(rows).filteredOn(r -> r.reversalOfId() != null).hasSize(3)
                .allSatisfy(r -> assertThat(r.quantity().signum()).isPositive());

        assertThat(stockApi.reverseConsumption(new ReverseConsumptionCommand(
                EMovementSourceType.POS_SALE_LINE, saleId, null, "again", STAFF))).isZero();
        assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("24");   // not restored twice

        assertLedgerConsistent(w.branchId(), false);
    }

    // ------------------------------------------------------------------ stocktake

    @Test
    void stocktake_with_a_variance_posts_an_adjustment_and_matches_the_count() {
        World w = newWorld();
        stockUp(w);

        UUID stocktake = stocktakes.start(w.bar().id(), STAFF);
        stocktakes.recordCount(stocktake, w.soda().id(), bd("20"), w.unit("PCS").id());   // 24 expected, 4 missing
        stocktakes.recordCount(stocktake, w.beef().id(), bd("2"), w.unit("KG").id());     // 2000 g expected: no variance
        stocktakes.submit(stocktake);
        UUID adjustment = stocktakes.approve(stocktake, STAFF);

        assertThat(adjustment).isNotNull();
        StockDocumentView doc = query.document(adjustment);
        assertThat(doc.documentType()).isEqualTo(EDocumentType.ADJUSTMENT);
        assertThat(doc.lines()).hasSize(1);                                  // only the line with a variance
        assertThat(doc.lines().get(0).itemCode()).isEqualTo(w.soda().code());
        assertThat(doc.lines().get(0).quantity()).isEqualByComparingTo("-4");

        assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("20");
        assertThat(onHand(w.bar().id()).get(w.bun().code())).isEqualByComparingTo("40");   // uncounted: untouched
        assertThat(query.stocktake(stocktake).status()).isEqualTo(EStocktakeStatus.APPROVED);
        assertThat(query.stocktake(stocktake).adjustmentDocumentId()).isEqualTo(adjustment);

        // an approved stocktake cannot be approved again, and a new one may be opened
        assertThatThrownBy(() -> stocktakes.approve(stocktake, STAFF)).isInstanceOf(StockRuleViolationException.class);
        assertThat(stocktakes.start(w.bar().id(), STAFF)).isNotNull();

        assertLedgerConsistent(w.branchId(), false);
    }

    // ------------------------------------------------------------------ branch scoping

    @Test
    void ids_from_another_branch_look_like_missing_ones() {
        World a = newWorld();
        World b = newWorld();
        UUID docOfB = receipt(b.main().id(), line(b.soda(), "10", b.unit("PCS")));
        UUID stocktakeOfB = stocktakes.start(b.bar().id(), STAFF);

        actAs(a.branchId());                                                   // now a user of branch A

        assertThatThrownBy(() -> receipt(b.main().id(), line(a.soda(), "1", a.unit("PCS"))))
                .isInstanceOf(StockRuleViolationException.class).hasMessageContaining("Unknown store");
        assertThatThrownBy(() -> operations.transfer(new TransferCommand(b.main().id(), b.bar().id(),
                ETransferPurpose.DISTRIBUTION, null, List.of(line(a.soda(), "1", a.unit("PCS"))), STAFF)))
                .isInstanceOf(StockRuleViolationException.class).hasMessageContaining("Unknown store");
        assertThatThrownBy(() -> stocktakes.start(b.main().id(), STAFF))
                .isInstanceOf(StockRuleViolationException.class);
        assertThatThrownBy(() -> stocktakes.submit(stocktakeOfB)).isInstanceOf(StockRuleViolationException.class);
        assertThatThrownBy(() -> query.document(docOfB)).isInstanceOf(StockNotFoundException.class);
        assertThatThrownBy(() -> query.stocktake(stocktakeOfB)).isInstanceOf(StockNotFoundException.class);
        assertThatThrownBy(() -> structure.renameStore(b.bar().id(), "hijacked")).isInstanceOf(StockNotFoundException.class);

        assertThat(structure.stores(true)).extracting(s -> s.branchId()).containsOnly(a.branchId());
        assertThat(query.stockOnHand(new StockOnHandFilter(b.main().id(), null, null, true), 0, 50).items()).isEmpty();
        assertThat(query.movements(new MovementFilter(null, null, null, null, null, docOfB, null, null), 0, 50)
                .items()).isEmpty();                                           // even when filtering by B's document id

        actAs(b.branchId());                                                   // B's own data is untouched
        assertThat(onHand(b.main().id()).get(b.soda().code())).isEqualByComparingTo("10");
    }

    // ------------------------------------------------------------------ auditing of the configuration entities

    @ParameterizedTest
    @ValueSource(strings = {"BranchStockSetting", "DocumentSequence", "ItemPackaging", "Recipe", "RecipeLine",
            "StockCategory", "Store", "StoreAssignment", "UnitOfMeasure"})
    void configuration_entities_are_audited(String entity) {
        newWorld();
        TransactionTemplate tx = new TransactionTemplate(txManager);
        long total = tx.execute(s -> em.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult());
        long unaudited = tx.execute(s -> em.createQuery(
                "select count(e) from " + entity + " e where e.createdByUserId is null", Long.class).getSingleResult());

        assertThat(total).as("rows in " + entity).isPositive();
        assertThat(unaudited).as(entity + " rows without a creator (does it extend Auditable?)").isZero();
    }
}