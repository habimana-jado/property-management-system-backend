package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.Optional;
import java.util.UUID;

/** Implemented by each outlet module so stock can check an outlet exists and which branch it belongs to,
 *  without depending on that module. One bean per OutletType. */
public interface OutletDirectory {
    EOutletType type();
    Optional<UUID> branchIdOf(UUID outletId);
}
