package rw.afriteck.pms.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import rw.afriteck.pms.auth.security.StaffPrincipal;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.common.enums.EPaymentMethod;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.payment.service.PaymentService;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.enums.ESaleStatus;
import rw.afriteck.pms.pos.service.POSProductCategoryService;
import rw.afriteck.pms.pos.service.POSProductService;
import rw.afriteck.pms.pos.service.POSRegisterSessionService;
import rw.afriteck.pms.pos.service.POSSaleService;
import rw.afriteck.pms.pos.service.POSTerminalService;
import rw.afriteck.pms.property.model.Hotel;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.restaurant.dtos.*;
import rw.afriteck.pms.restaurant.enums.EBillStatus;
import rw.afriteck.pms.restaurant.enums.EMenuItemType;
import rw.afriteck.pms.restaurant.enums.EPackageType;
import rw.afriteck.pms.restaurant.enums.ETableBillItemStatus;
import rw.afriteck.pms.restaurant.enums.ETableStatus;
import rw.afriteck.pms.restaurant.model.TableBillItem;
import rw.afriteck.pms.restaurant.repository.TableBillItemRepo;
import rw.afriteck.pms.restaurant.repository.TableBillRepo;
import rw.afriteck.pms.restaurant.repository.TableMasterRepo;
import rw.afriteck.pms.restaurant.service.MenuCategoryService;
import rw.afriteck.pms.restaurant.service.MenuMasterService;
import rw.afriteck.pms.restaurant.service.OrderService;
import rw.afriteck.pms.restaurant.service.RestaurantService;
import rw.afriteck.pms.restaurant.service.TableMasterService;
import rw.afriteck.pms.stock.api.CatalogSetupApi;
import rw.afriteck.pms.stock.api.RecipeSetupApi;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.StockQueryApi;
import rw.afriteck.pms.stock.api.StructureSetupApi;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EItemType;
import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.api.query.MovementFilter;
import rw.afriteck.pms.stock.api.query.StockOnHandFilter;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.api.view.MovementRow;
import rw.afriteck.pms.stock.api.view.StockOnHandRow;

/**
 * The whole application, real PostgreSQL, real services (no HTTP): a POS sale and a restaurant order must move
 * stock, roll back with their payment/order when stock refuses, and a void must give stock back even after the
 * table was split or merged. NOT @Transactional: every service call commits or rolls back on its own.
 *
 * Opt-in: PMS_IT_DB_URL (a DEDICATED database whose name contains "test", e.g. pms_app_test: the schema is dropped
 * and recreated), optional PMS_IT_DB_USER / PMS_IT_DB_PASSWORD (default postgres / postgres).
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "PMS_IT_DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate.SQL=WARN",
        "logging.level.org.hibernate.stat=WARN",
        "app.jwt.secret=integration-test-secret-integration-test-secret-0123456789",
        "pms.pos.upload.product-image-dir=${java.io.tmpdir}/pms-it/product-images",
        "pms.pos.upload.max-file-size-bytes=2097152"
})
@ActiveProfiles("it")
class PosRestaurantStockEndToEndTest {

    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-0000000000b1");
    private static final UUID STAFF = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("PMS_IT_DB_URL");
        if (url == null || !url.toLowerCase().contains("test")) {
            throw new IllegalStateException("PMS_IT_DB_URL must point to a dedicated TEST database "
                    + "(its name must contain 'test'): the schema is dropped and recreated.");
        }
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("PMS_IT_DB_USER", "postgres"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("PMS_IT_DB_PASSWORD", "postgres"));
    }

    // stock
    @Autowired StructureSetupApi structure;
    @Autowired CatalogSetupApi catalog;
    @Autowired RecipeSetupApi recipes;
    @Autowired StockOperations operations;
    @Autowired StockQueryApi query;
    // pos
    @Autowired POSTerminalService terminals;
    @Autowired POSProductCategoryService posCategories;
    @Autowired POSProductService posProducts;
    @Autowired POSRegisterSessionService sessions;
    @Autowired POSSaleService sales;
    // restaurant
    @Autowired RestaurantService restaurants;
    @Autowired TableMasterService tables;
    @Autowired MenuCategoryService menuCategories;
    @Autowired MenuMasterService menuItems;
    @Autowired OrderService orders;
    @Autowired TableBillRepo billRepo;
    @Autowired TableBillItemRepo billItemRepo;
    @Autowired TableMasterRepo tableRepo;
    // payment + infrastructure
    @Autowired PaymentService payments;
    @Autowired PlatformTransactionManager txManager;
    @PersistenceContext EntityManager em;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ================================================================== POS

    @Test
    void pos_sale_deducts_the_terminals_store_and_records_the_payment() {
        World w = newWorld();
        UUID saleId = newSale(w);
        sales.addItem(saleId, new AddSaleItemRequest(w.sodaProductId(), 3));        // 3 x 1000, tracked
        POSSaleResponse withService = sales.addItem(saleId, new AddSaleItemRequest(w.serviceProductId(), 1)); // 500, untracked
        BigDecimal total = withService.grandTotal();
        assertThat(total).isEqualByComparingTo("3500");

        POSSaleResponse done = sales.complete(saleId,
                new CompleteSaleRequest(List.of(new TenderRequest(EPaymentMethod.CASH, total, "till-1"))));

        assertThat(done.status()).isEqualTo(ESaleStatus.COMPLETED);
        assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("21");        // 24 - 3
        UUID sodaLine = done.items().stream().filter(i -> i.productId().equals(w.sodaProductId()))
                .findFirst().orElseThrow().id();
        List<MovementRow> movements = movementsOf(EMovementSourceType.POS_SALE_LINE, saleId);
        assertThat(movements).hasSize(1);                                                       // the service line moved nothing
        assertThat(movements.get(0).movementType()).isEqualTo(EMovementType.SALE);
        assertThat(movements.get(0).sourceLineId()).isEqualTo(sodaLine);
        assertThat(movements.get(0).performedBy()).isEqualTo(STAFF);
        assertThat(payments.sumPaidAmount(saleId, EPayableType.POS_SALE)).isEqualByComparingTo(total);
        assertLedgerConsistent(w.branchId());
    }

    @Test
    void pos_sale_that_exceeds_stock_rolls_back_the_payment_and_leaves_the_sale_open() {
        World w = newWorld();
        UUID saleId = newSale(w);
        BigDecimal total = sales.addItem(saleId, new AddSaleItemRequest(w.sodaProductId(), 30)).grandTotal();  // 24 on hand

        assertThatThrownBy(() -> sales.complete(saleId,
                new CompleteSaleRequest(List.of(new TenderRequest(EPaymentMethod.CASH, total, null)))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(sales.findById(saleId).status()).isEqualTo(ESaleStatus.DRAFT);
        assertThat(payments.sumPaidAmount(saleId, EPayableType.POS_SALE)).isEqualByComparingTo("0");
        assertThat(onHand(w.bar().id()).get(w.soda().code())).isEqualByComparingTo("24");
        assertThat(movementsOf(EMovementSourceType.POS_SALE_LINE, saleId)).isEmpty();
        assertLedgerConsistent(w.branchId());
    }

    @Test
    void pos_product_mapping_is_validated() {
        World w = newWorld();

        assertThatThrownBy(() -> newPosProduct(w.branchId(), w.posCategoryId(),
                "BAD-" + w.tag(), "Tracked but unmapped", "100", null, true))
                .isInstanceOf(StockRuleViolationException.class);
        assertThatThrownBy(() -> newPosProduct(w.branchId(), w.posCategoryId(),
                "GHOST-" + w.tag(), "Maps to nothing", "100", UUID.randomUUID(), true))
                .isInstanceOf(StockRuleViolationException.class);
    }

    @Test
    void a_terminal_of_another_branch_cannot_start_a_sale() {
        World a = newWorld();
        World b = newWorld();                         // acting as B now, whose terminal has an open session
        actAs(a.branchId());

        assertThatThrownBy(() -> sales.create(new CreateSaleRequest(b.terminalId(), null)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // ================================================================== restaurant

    @Test
    void restaurant_order_deducts_recipe_ingredients_and_a_void_gives_them_back() {
        World w = newWorld();

        orders.placeOrder(w.tableA(), order(line(w.burgerMenuId(), 2), line(w.sodaMenuId(), 1), line(w.waterMenuId(), 1)));

        UUID billId = activeBillId(w.tableA());
        Map<String, BigDecimal> bar = onHand(w.bar().id());
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("1700");     // 2000 - 2 * 150
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("38");        // 40 - 2
        assertThat(bar.get(w.soda().code())).isEqualByComparingTo("23");       // 24 - 1
        assertThat(movementsOf(EMovementSourceType.TABLE_BILL_LINE, billId)).hasSize(3);   // water moved nothing

        Map<UUID, UUID> lines = activeLines(billId);
        orders.voidItem(lines.get(w.burgerMenuId()), new VoidItemRequest("customer changed their mind"));

        bar = onHand(w.bar().id());
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("2000");
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("40");
        assertThat(bar.get(w.soda().code())).isEqualByComparingTo("23");       // the soda line stays
        assertThat(movementsOf(EMovementSourceType.TABLE_BILL_LINE, billId)).hasSize(5);   // 3 sales + 2 reversals

        assertThatThrownBy(() -> orders.voidItem(lines.get(w.burgerMenuId()), new VoidItemRequest("again")))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThat(onHand(w.bar().id()).get(w.beef().code())).isEqualByComparingTo("2000");   // not given back twice
        assertLedgerConsistent(w.branchId());
    }

    @Test
    void a_void_after_a_split_still_gives_the_stock_back() {
        World w = newWorld();
        orders.placeOrder(w.tableA(), order(line(w.burgerMenuId(), 1), line(w.sodaMenuId(), 1)));
        UUID originalBill = activeBillId(w.tableA());
        UUID burgerLine = activeLines(originalBill).get(w.burgerMenuId());

        orders.splitTable(w.tableA(), new SplitTableRequest(w.tableB(), List.of(burgerLine)));   // burger moves to table B
        UUID newBill = activeBillId(w.tableB());
        assertThat(newBill).isNotEqualTo(originalBill);

        orders.voidItem(burgerLine, new VoidItemRequest("wrong table, not wanted"));

        Map<String, BigDecimal> bar = onHand(w.bar().id());
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("2000");      // the movement still names the OLD bill
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("40");
        assertThat(bar.get(w.soda().code())).isEqualByComparingTo("23");
        assertLedgerConsistent(w.branchId());
    }

    @Test
    void a_void_after_a_merge_still_gives_the_stock_back() {
        World w = newWorld();
        orders.placeOrder(w.tableA(), order(line(w.sodaMenuId(), 1)));
        orders.placeOrder(w.tableB(), order(line(w.burgerMenuId(), 1)));
        UUID burgerLine = activeLines(activeBillId(w.tableB())).get(w.burgerMenuId());

        orders.mergeTable(w.tableA(), new MergeTableRequest(w.tableB()));                      // B's lines move onto A's bill
        orders.voidItem(burgerLine, new VoidItemRequest("sent back"));

        Map<String, BigDecimal> bar = onHand(w.bar().id());
        assertThat(bar.get(w.beef().code())).isEqualByComparingTo("2000");
        assertThat(bar.get(w.bun().code())).isEqualByComparingTo("40");
        assertThat(bar.get(w.soda().code())).isEqualByComparingTo("23");
        assertLedgerConsistent(w.branchId());
    }

    @Test
    void an_order_that_exceeds_stock_is_rejected_and_nothing_is_saved() {
        World w = newWorld();

        assertThatThrownBy(() -> orders.placeOrder(w.tableA(), order(line(w.burgerMenuId(), 20))))   // needs 3000 g, 2000 on hand
                .isInstanceOf(InsufficientStockException.class);

        assertThat(billRepo.findActiveBillByTableId(w.tableA(),
                List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED, EBillStatus.PARTIALLY_PAID))).isEmpty();
        assertThat(tableRepo.findById(w.tableA()).orElseThrow().getTableStatus()).isEqualTo(ETableStatus.AVAILABLE);
        assertThat(onHand(w.bar().id()).get(w.beef().code())).isEqualByComparingTo("2000");
        assertLedgerConsistent(w.branchId());
    }

    @Test
    void menu_item_mapping_is_validated() {
        World w = newWorld();

        assertThatThrownBy(() -> menuItems.register(new CreateMenuMasterRequest("Both", new BigDecimal("100"),
                EMenuItemType.FOOD, EPackageType.PLATE, w.menuCategoryId(), w.soda().id(), w.burger().id(), true)))
                .isInstanceOf(StockRuleViolationException.class);                              // stock item AND recipe
        assertThatThrownBy(() -> menuItems.register(new CreateMenuMasterRequest("Neither", new BigDecimal("100"),
                EMenuItemType.FOOD, EPackageType.PLATE, w.menuCategoryId(), null, null, true)))
                .isInstanceOf(StockRuleViolationException.class);                              // tracked but unmapped
    }

    // ================================================================== world

    private record World(String tag, UUID branchId, StoreView main, StoreView bar, UUID terminalId, UUID restaurantId,
                         UUID tableA, UUID tableB, Map<String, UnitView> units, CategoryView category,
                         ItemView beef, ItemView bun, ItemView soda, RecipeView burger,
                         UUID posCategoryId, UUID sodaProductId, UUID serviceProductId,
                         UUID menuCategoryId, UUID burgerMenuId, UUID sodaMenuId, UUID waterMenuId) {
        UnitView unit(String code) { return units.get(code); }
    }

    /**
     * A fresh branch with the whole chain wired: stores (MAIN + bar), a POS terminal with an open session and a
     * restaurant (both assigned to the bar), stock items and a burger recipe, POS products and menu items mapped to
     * them, and the bar stocked with 2000 g beef, 24 sodas and 40 buns. Leaves the security context on this branch.
     */
    private World newWorld() {
        String tag = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        UUID branchId = new TransactionTemplate(txManager).execute(status -> {
            Hotel hotel = new Hotel();
            hotel.setHotelName("Hotel " + tag);
            hotel.setEmail(tag.toLowerCase() + "@hotel.test");
            hotel.setStatus(ERecordStatus.ACTIVE);
            em.persist(hotel);
            HotelBranch branch = new HotelBranch();
            branch.setName("Branch " + tag);
            branch.setLocation("Kigali");
            branch.setStatus(ERecordStatus.ACTIVE);
            branch.setHotel(hotel);
            em.persist(branch);
            return branch.getId();
        });
        actAs(branchId);

        // stock structure
        StoreView main = structure.createStore(new StoreCommand("MAIN-" + tag, "Main store", EStoreType.MAIN));
        StoreView bar = structure.createStore(new StoreCommand("BAR-" + tag, "Bar store", EStoreType.DEPARTMENT));

        // outlets, assigned to the bar store
        UUID terminalId = terminals.create(new CreateTerminalRequest(branchId, "T" + tag, "Till " + tag)).id();
        UUID restaurantId = restaurants.create(new CreateRestaurantRequest(
                "Resto " + tag, "TIN" + tag, tag.substring(0, 6), branchId)).id();
        structure.assign(new AssignmentCommand(bar.id(), EOutletType.POS_TERMINAL, terminalId, true));
        structure.assign(new AssignmentCommand(bar.id(), EOutletType.RESTAURANT, restaurantId, true));
        sessions.open(new OpenRegisterSessionRequest(terminalId, BigDecimal.ZERO));
        UUID tableA = tables.register(new CreateTableMasterRequest("A-" + tag, 4, restaurantId)).id();
        UUID tableB = tables.register(new CreateTableMasterRequest("B-" + tag, 4, restaurantId)).id();

        // stock catalog
        Map<String, UnitView> units = catalog.units().stream().collect(Collectors.toMap(UnitView::code, Function.identity()));
        CategoryView category = catalog.createCategory(new CategoryCommand("CAT-" + tag, "Test category", null));
        ItemView beef = catalog.createItem(new ItemCommand("BEEF-" + tag, "Beef", null,
                category.id(), units.get("G").id(), EItemType.INGREDIENT, null));
        ItemView bun = catalog.createItem(new ItemCommand("BUN-" + tag, "Bun", null,
                category.id(), units.get("PCS").id(), EItemType.INGREDIENT, null));
        ItemView soda = catalog.createItem(new ItemCommand("SODA-" + tag, "Soda", null,
                category.id(), units.get("PCS").id(), EItemType.SELLABLE, null));
        catalog.putPackaging(soda.id(), new PackagingCommand(units.get("CARTON").id(), new BigDecimal("24")));
        RecipeView burger = recipes.createRecipe(new RecipeCommand("Burger " + tag, List.of(
                new RecipeLineCommand(beef.id(), new BigDecimal("150"), units.get("G").id()),
                new RecipeLineCommand(bun.id(), BigDecimal.ONE, units.get("PCS").id()))));

        // POS products and menu items, mapped to stock
        UUID posCategoryId = posCategories.create(new CreatePOSProductCategoryRequest(branchId, "Drinks " + tag)).id();
        UUID sodaProductId = newPosProduct(branchId, posCategoryId, "SODA-" + tag, "Soda", "1000", soda.id(), true).id();
        UUID serviceProductId = newPosProduct(branchId, posCategoryId, "SVC-" + tag, "Service charge", "500", null, false).id();
        UUID menuCategoryId = menuCategories.register(new CreateMenuCategoryRequest("Drinks", restaurantId, null)).id();
        UUID burgerMenuId = menuItems.register(new CreateMenuMasterRequest("Burger", new BigDecimal("5000"),
                EMenuItemType.FOOD, EPackageType.PLATE, menuCategoryId, null, burger.id(), true)).id();
        UUID sodaMenuId = menuItems.register(new CreateMenuMasterRequest("Soda", new BigDecimal("1000"),
                EMenuItemType.BEVERAGE, EPackageType.BOTTLE, menuCategoryId, soda.id(), null, true)).id();
        UUID waterMenuId = menuItems.register(new CreateMenuMasterRequest("Table water", new BigDecimal("500"),
                EMenuItemType.BEVERAGE, EPackageType.BOTTLE, menuCategoryId, null, null, false)).id();

        World w = new World(tag, branchId, main, bar, terminalId, restaurantId, tableA, tableB, units, category,
                beef, bun, soda, burger, posCategoryId, sodaProductId, serviceProductId,
                menuCategoryId, burgerMenuId, sodaMenuId, waterMenuId);
        stockUp(w);
        return w;
    }

    /** 5 kg beef, 2 cartons of soda, 100 buns into MAIN; then 2 kg beef, 24 sodas, 40 buns to the bar. */
    private void stockUp(World w) {
        operations.postDocument(new SingleStoreDocumentCommand(EDocumentType.RECEIPT, w.main().id(),
                "Acme Foods", "DN-" + w.tag(), null, List.of(
                input(w.beef(), "5", w.unit("KG")), input(w.soda(), "2", w.unit("CARTON")), input(w.bun(), "100", w.unit("PCS"))),
                STAFF));
        operations.transfer(new TransferCommand(w.main().id(), w.bar().id(), ETransferPurpose.DISTRIBUTION, "opening", List.of(
                input(w.beef(), "2", w.unit("KG")), input(w.soda(), "24", w.unit("PCS")), input(w.bun(), "40", w.unit("PCS"))),
                STAFF));
    }

    // ================================================================== helpers

    /** The caller becomes a staff member of the given branch, exactly as the JWT filter would set it up. */
    private void actAs(UUID branchId) {
        StaffPrincipal principal = new StaffPrincipal(USER, STAFF, "e2e-tester",null, branchId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    /** The only place that knows the component order of CreatePOSProductRequest. */
    private POSProductResponse newPosProduct(UUID branchId, UUID categoryId, String sku, String name, String price,
                                             UUID stockItemId, boolean trackInventory) {
        return posProducts.create(new CreatePOSProductRequest(branchId, categoryId, sku, name, null,
                new BigDecimal(price), trackInventory, stockItemId));
    }

    private UUID newSale(World w) {
        return sales.create(new CreateSaleRequest(w.terminalId(), null)).id();
    }

    private static DocumentLineInput input(ItemView item, String quantity, UnitView unit) {
        return new DocumentLineInput(item.id(), new BigDecimal(quantity), unit.id());
    }

    private static PlaceOrderRequest order(OrderLineRequest... lines) {
        return new PlaceOrderRequest(List.of(lines));
    }

    private static OrderLineRequest line(UUID menuItemId, int quantity) {
        return new OrderLineRequest(menuItemId, quantity, null);
    }

    private UUID activeBillId(UUID tableId) {
        return billRepo.findActiveBillByTableId(tableId,
                List.of(EBillStatus.OPEN, EBillStatus.BILL_REQUESTED, EBillStatus.PARTIALLY_PAID)).orElseThrow().getId();
    }

    /** menu item id -> bill line id, for the active lines of a bill. */
    private Map<UUID, UUID> activeLines(UUID billId) {
        return new TransactionTemplate(txManager).execute(status ->
                billItemRepo.findByTableBillIdAndStatus(billId, ETableBillItemStatus.ACTIVE).stream()
                        .collect(Collectors.toMap(i -> i.getMenuMaster().getId(), TableBillItem::getId)));
    }

    private Map<String, BigDecimal> onHand(UUID storeId) {
        return query.stockOnHand(new StockOnHandFilter(storeId, null, null, true), 0, 200).items().stream()
                .collect(Collectors.toMap(StockOnHandRow::itemCode, StockOnHandRow::quantityOnHand));
    }

    private List<MovementRow> movementsOf(EMovementSourceType type, UUID sourceId) {
        return query.movements(new MovementFilter(null, null, null, null, null, null, type, sourceId), 0, 100).items();
    }

    /** Every balance equals the sum of its movements and nothing is negative. */
    private void assertLedgerConsistent(UUID branchId) {
        List<Object[]> rows = new TransactionTemplate(txManager).execute(status -> em.createQuery("""
                        select b.quantityOnHand,
                               coalesce((select sum(m.quantity) from StockMovement m
                                         where m.store = b.store and m.item = b.item), 0)
                        from StockBalance b where b.store.branch.id = :branchId""", Object[].class)
                .setParameter("branchId", branchId).getResultList());
        assertThat(rows).isNotEmpty();
        for (Object[] row : rows) {
            BigDecimal balance = new BigDecimal(row[0].toString());
            assertThat(balance).as("balance must equal the sum of its movements")
                    .isEqualByComparingTo(new BigDecimal(row[1].toString()));
            assertThat(balance.signum()).as("balance must not be negative").isGreaterThanOrEqualTo(0);
        }
    }
}