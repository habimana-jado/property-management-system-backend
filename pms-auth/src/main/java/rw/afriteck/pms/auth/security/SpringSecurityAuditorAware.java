package rw.afriteck.pms.auth.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.auth.dtos.PlatformPrincipal;

import java.util.Optional;
import java.util.UUID;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof StaffPrincipal staff) {
            return Optional.of(staff.userId());
        }
        if (principal instanceof PlatformPrincipal operator) {
            return Optional.of(operator.operatorId());
        }
        return Optional.empty();   // anonymous ("anonymousUser" is a String) or an unknown principal type
    }
}
