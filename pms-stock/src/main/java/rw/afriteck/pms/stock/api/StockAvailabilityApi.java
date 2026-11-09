package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.view.Availability;

import java.util.Collection;
import java.util.UUID;

/** For pms-pos and pms-restaurant screens. Advisory only: it is stale the moment it is read.
 *  The real guard is consumeForSale. Unknown recipe ids are simply absent from the result. */
public interface StockAvailabilityApi {
    Availability availability(EOutletType outletType, UUID outletId, UUID storeId,
                              Collection<UUID> stockItemIds, Collection<UUID> recipeIds);
}
