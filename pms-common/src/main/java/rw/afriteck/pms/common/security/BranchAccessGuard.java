package rw.afriteck.pms.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.ForbiddenException;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BranchAccessGuard {

    private final CurrentUserService currentUserService;

    /** Empty for hotel-wide accounts (no branch in the token). */
    public Optional<UUID> restrictedBranchId() {
        return Optional.ofNullable(currentUserService.requireCurrentHotelBranchId());
    }

    /** Branch staff may only act on their own branch. A resource with no branch is hotel-wide only. */
    public void requireAccessTo(UUID branchId) {
        UUID mine = currentUserService.requireCurrentHotelBranchId();
        if (mine != null && !mine.equals(branchId)) {
            throw new ForbiddenException("BRANCH_MISMATCH", "You are not assigned to this branch");
        }
    }
}
