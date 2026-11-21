package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.security.BranchAccessGuard;
import rw.afriteck.pms.common.tenant.HotelDirectory;
import rw.afriteck.pms.common.tenant.TenantContext;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class PosBranchAccess {

    private final BranchAccessGuard guard;
    private final HotelDirectory hotelDirectory;

    /** For a branch id from a request body: it must belong to this hotel, and to this user. */
    void requireBranch(UUID branchId) {
        if (!hotelDirectory.branchBelongsToHotel(branchId, TenantContext.require())) {
            throw new ResourceNotFoundException("Hotel Branch", branchId);
        }
        guard.requireAccessTo(branchId);
    }

    /** For a branch id read from a record that is already loaded (it is in this hotel by construction). */
    void require(UUID branchId) {
        guard.requireAccessTo(branchId);
    }
}
