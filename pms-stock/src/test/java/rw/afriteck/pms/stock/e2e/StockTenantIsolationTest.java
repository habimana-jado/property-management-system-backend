package rw.afriteck.pms.stock.e2e;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.transaction.support.TransactionTemplate;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.tenant.TenantContext;
import rw.afriteck.pms.stock.api.enums.EItemType;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.api.setup.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "PMS_IT_DB_URL", matches = ".+")
class StockTenantIsolationTest extends AbstractStockIntegrationTest {

    @Test
    void sameCatalogCodesAreAllowedInDifferentHotels() {
        World a = newWorld(newHotel());
        World b = newWorld(newHotel());          // switches the tenant and the acting branch to hotel B

        // hotel A's codes, reused in hotel B: a global unique violation before this step
        CategoryView category = catalog.createCategory(new CategoryCommand("CAT-" + a.tag(), "Same code", null));
        ItemView item = catalog.createItem(new ItemCommand("BEEF-" + a.tag(), "Beef", null,
                category.id(), b.unit("G").id(), EItemType.INGREDIENT, null));

        assertThat(item.code()).isEqualTo("BEEF-" + a.tag());
        assertThat(item.id()).isNotEqualTo(a.beef().id());
    }

    @Test
    void anotherHotelSeesNothingOfTheCatalogStoresOrStock() {
        UUID hotelA = newHotel();
        World a = newWorld(hotelA);
        stockUp(a);
        newWorld(newHotel());                    // now acting in hotel B

        assertThat(counts(a)).containsOnly(0L);  // hotel B sees none of hotel A's rows

        TenantContext.set(hotelA);               // sanity: the owner does see them
        assertThat(counts(a)).allMatch(count -> count > 0);
    }

    @Test
    void unitsAreSharedByEveryHotel() {
        World a = newWorld(newHotel());
        World b = newWorld(newHotel());

        assertThat(b.unit("KG").id()).isEqualTo(a.unit("KG").id());
    }

    @Test
    void anAccountWithNoBranchCannotTouchStores() {
        newWorld();                                                        // sets the tenant
        when(currentUser.requireCurrentHotelBranchId()).thenReturn(null);  // a hotel-wide administrator

        assertThatThrownBy(() -> structure.createStore(new StoreCommand("X-1", "Nope", EStoreType.DEPARTMENT)))
                .isInstanceOf(ForbiddenException.class);
    }

    /** Rows of one world that the current tenant can see: store, item, balances, movements. */
    private List<Long> counts(World w) {
        return new TransactionTemplate(txManager).execute(status -> List.of(
                em.createQuery("select count(s) from Store s where s.id = :id", Long.class)
                        .setParameter("id", w.main().id()).getSingleResult(),
                em.createQuery("select count(i) from StockItem i where i.id = :id", Long.class)
                        .setParameter("id", w.beef().id()).getSingleResult(),
                em.createQuery("select count(b) from StockBalance b where b.store.id = :id", Long.class)
                        .setParameter("id", w.main().id()).getSingleResult(),
                em.createQuery("select count(m) from StockMovement m where m.store.id = :id", Long.class)
                        .setParameter("id", w.main().id()).getSingleResult()));
    }
}
