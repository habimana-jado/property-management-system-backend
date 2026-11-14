package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.security.CurrentUserService; // adjust to the real package in pms-common
import rw.afriteck.pms.stock.internal.model.Store;

import java.util.UUID;

/**
 * Branch scoping for the web-facing stock services.
 *
 * The branch always comes from the authenticated user's token, never from request input.
 * Entities that belong to another branch are treated exactly like entities that do not exist
 * (each service throws its usual "unknown/not found" error), so ids cannot be probed across branches.
 *
 * Not used by StockApi (consumeForSale / reverseConsumption): the outlet modules scope their own sales.
 * Catalog data (units, categories, items, recipes) is shared by all branches of the deployment on purpose.
 */
@Component
@RequiredArgsConstructor
class BranchGuard {

    private final CurrentUserService currentUser;

    /** The caller's branch, from the token. Throws if there is no authenticated user or no branch claim. */
    UUID currentBranchId() {
        return currentUser.requireCurrentHotelBranchId();
    }

    /** True when the store belongs to the caller's branch. */
    boolean inCurrentBranch(Store store) {
        return currentBranchId().equals(store.getBranch().getId());
    }
}