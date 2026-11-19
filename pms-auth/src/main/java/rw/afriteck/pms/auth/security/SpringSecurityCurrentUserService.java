package rw.afriteck.pms.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.AuthenticationRequiredException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.security.CurrentUserService;

import java.util.UUID;

@Component
public class SpringSecurityCurrentUserService implements CurrentUserService {

    @Override
    public UUID requireCurrentStaffId() {
        return requirePrincipal().staffId();
    }

    @Override
    public UUID requireCurrentUserId() {
        return requirePrincipal().userId();
    }

    @Override
    public UUID requireCurrentHotelId() {
        UUID hotelId = requirePrincipal().hotelId();
        if (hotelId == null) {
            throw new ForbiddenException("NO_TENANT", "This account is not attached to a hotel");
        }
        return hotelId;
    }

    @Override
    public UUID requireCurrentHotelBranchId() {
        return requirePrincipal().hotelBranchId();
    }

    private StaffPrincipal requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof StaffPrincipal principal)) {
            throw new AuthenticationRequiredException("No authenticated staff user in security context");
        }

        return principal;
    }
}
