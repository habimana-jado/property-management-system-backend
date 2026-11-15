package rw.afriteck.pms.stock.e2e;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import rw.afriteck.pms.StockTestApplication;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.security.CurrentUserService;
import rw.afriteck.pms.property.model.Hotel;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.CatalogSetupApi;
import rw.afriteck.pms.stock.api.RecipeSetupApi;
import rw.afriteck.pms.stock.api.StockApi;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.StockQueryApi;
import rw.afriteck.pms.stock.api.StocktakeApi;
import rw.afriteck.pms.stock.api.StructureSetupApi;
import rw.afriteck.pms.stock.api.command.DocumentLineInput;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EItemType;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.api.enums.ETransferPurpose;
import rw.afriteck.pms.stock.api.query.StockOnHandFilter;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.api.view.StockOnHandRow;

/**
 * Real PostgreSQL, real services, no HTTP. NOT @Transactional on purpose: every service call commits (or rolls
 * back) on its own, exactly as in production, so locking, the separate balance-creation transaction and
 * rollback-on-exception are all exercised.
 *
 * Opt-in: set PMS_IT_DB_URL (a DEDICATED test database whose name contains "test"; the schema is dropped and
 * recreated), and optionally PMS_IT_DB_USER / PMS_IT_DB_PASSWORD (default postgres / postgres).
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "PMS_IT_DB_URL", matches = ".+")
@SpringBootTest(classes = StockTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.open-in-view=false"
        })
@Import(IntegrationTestBeans.class)
abstract class AbstractStockIntegrationTest {

    protected static final UUID STAFF = UUID.fromString("00000000-0000-0000-0000-0000000000a1");

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

    @MockitoBean protected CurrentUserService currentUser;

    @Autowired protected StructureSetupApi structure;
    @Autowired protected CatalogSetupApi catalog;
    @Autowired protected RecipeSetupApi recipes;
    @Autowired protected StockOperations operations;
    @Autowired protected StockApi stockApi;
    @Autowired protected StocktakeApi stocktakes;
    @Autowired protected StockQueryApi query;
    @Autowired protected IntegrationTestBeans.TestOutlets outlets;
    @Autowired protected PlatformTransactionManager txManager;
    @PersistenceContext protected EntityManager em;

    // ------------------------------------------------------------------ the world one test works in

    protected record World(String tag, UUID branchId, StoreView main, StoreView bar, UUID terminalId, UUID restaurantId,
                           Map<String, UnitView> units, CategoryView category,
                           ItemView beef, ItemView bun, ItemView soda, RecipeView burger) {
        UnitView unit(String code) { return units.get(code); }
    }

    /**
     * A fresh hotel branch with a MAIN store, a bar store, a POS terminal and a restaurant both assigned to the
     * bar, three items (beef in G, bun in PCS, soda in PCS with a 24-piece carton) and a burger recipe
     * (150 G beef + 1 bun). Leaves the mocked user acting as this new branch.
     */
    protected World newWorld() {
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

        StoreView main = structure.createStore(new StoreCommand("MAIN-" + tag, "Main store", EStoreType.MAIN));
        StoreView bar = structure.createStore(new StoreCommand("BAR-" + tag, "Bar store", EStoreType.DEPARTMENT));

        UUID terminalId = outlets.register(UUID.randomUUID(), branchId);
        UUID restaurantId = outlets.register(UUID.randomUUID(), branchId);
        structure.assign(new AssignmentCommand(bar.id(), EOutletType.POS_TERMINAL, terminalId, true));
        structure.assign(new AssignmentCommand(bar.id(), EOutletType.RESTAURANT, restaurantId, true));

        Map<String, UnitView> units = catalog.units().stream()
                .collect(Collectors.toMap(UnitView::code, Function.identity()));
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

        return new World(tag, branchId, main, bar, terminalId, restaurantId, units, category, beef, bun, soda, burger);
    }

    /** Makes the mocked token belong to the given branch (call again to switch branches mid-test). */
    protected void actAs(UUID branchId) {
        when(currentUser.requireCurrentHotelBranchId()).thenReturn(branchId);
        when(currentUser.requireCurrentStaffId()).thenReturn(STAFF);
        when(currentUser.requireCurrentUserId()).thenReturn(StockTestApplication.TEST_USER);
    }

    /**
     * Receipt of 5 kg beef, 2 cartons of soda and 100 buns into MAIN, then a distribution of 2 kg beef, 24 sodas and
     * 40 buns to the bar. Leaves MAIN with 3000 g / 24 / 60 and the bar with 2000 g / 24 / 40.
     */
    protected void stockUp(World w) {
        receipt(w.main().id(),
                line(w.beef(), "5", w.unit("KG")),
                line(w.soda(), "2", w.unit("CARTON")),
                line(w.bun(), "100", w.unit("PCS")));
        operations.transfer(new TransferCommand(w.main().id(), w.bar().id(), ETransferPurpose.DISTRIBUTION,
                "opening distribution", List.of(
                line(w.beef(), "2", w.unit("KG")),
                line(w.soda(), "24", w.unit("PCS")),
                line(w.bun(), "40", w.unit("PCS"))), STAFF));
    }

    // ------------------------------------------------------------------ small helpers

    protected UUID receipt(UUID storeId, DocumentLineInput... lines) {
        return operations.postDocument(new SingleStoreDocumentCommand(EDocumentType.RECEIPT, storeId,
                "Acme Foods", "DN-" + UUID.randomUUID().toString().substring(0, 6), null, List.of(lines), STAFF));
    }

    protected UUID openingBalance(UUID storeId, DocumentLineInput... lines) {
        return operations.postDocument(new SingleStoreDocumentCommand(EDocumentType.OPENING_BALANCE, storeId,
                null, null, null, List.of(lines), STAFF));
    }

    protected static DocumentLineInput line(ItemView item, String quantity, UnitView unit) {
        return new DocumentLineInput(item.id(), new BigDecimal(quantity), unit.id());
    }

    protected static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    /** Quantity on hand per item code for one store (zero rows included). */
    protected Map<String, BigDecimal> onHand(UUID storeId) {
        return query.stockOnHand(new StockOnHandFilter(storeId, null, null, true), 0, 200).items().stream()
                .collect(Collectors.toMap(StockOnHandRow::itemCode, StockOnHandRow::quantityOnHand));
    }

    /**
     * Ledger invariants for a branch: every balance equals the sum of its movements, and nothing is negative
     * (unless the branch allows negative stock, in which case pass allowNegative = true).
     */
    protected void assertLedgerConsistent(UUID branchId, boolean allowNegative) {
        List<Object[]> rows = new TransactionTemplate(txManager).execute(status -> em.createQuery("""
                        select b.quantityOnHand,
                               coalesce((select sum(m.quantity) from StockMovement m
                                         where m.store = b.store and m.item = b.item), 0)
                        from StockBalance b where b.store.branch.id = :branchId""", Object[].class)
                .setParameter("branchId", branchId).getResultList());

        org.assertj.core.api.Assertions.assertThat(rows).isNotEmpty();
        for (Object[] row : rows) {
            BigDecimal balance = new BigDecimal(row[0].toString());
            BigDecimal ledger = new BigDecimal(row[1].toString());
            org.assertj.core.api.Assertions.assertThat(balance)
                    .as("balance must equal the sum of its movements").isEqualByComparingTo(ledger);
            if (!allowNegative) {
                org.assertj.core.api.Assertions.assertThat(balance.signum())
                        .as("balance must not be negative").isGreaterThanOrEqualTo(0);
            }
        }
    }

    /** Runs the tasks at the same instant on separate threads; the result holds null for success, else the failure. */
    protected static List<Throwable> runConcurrently(List<Callable<?>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
        try {
            CountDownLatch ready = new CountDownLatch(tasks.size());
            CountDownLatch go = new CountDownLatch(1);
            List<Future<Throwable>> futures = new ArrayList<>();
            for (Callable<?> task : tasks) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    try {
                        task.call();
                        return null;
                    } catch (Throwable t) {
                        return t;
                    }
                }));
            }
            ready.await();
            go.countDown();
            List<Throwable> results = new ArrayList<>();
            for (Future<Throwable> f : futures) {
                results.add(f.get(60, TimeUnit.SECONDS));      // a timeout here means a deadlock
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    protected static long successes(List<Throwable> results) {
        return Collections.frequency(results, null);
    }
}