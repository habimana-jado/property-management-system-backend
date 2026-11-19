package rw.afriteck.pms.auth.enums;

import java.util.Set;

public enum EPlatformRole {
    PLATFORM_ADMIN(Set.of("PLATFORM_VIEW_HOTELS", "PLATFORM_MANAGE_HOTELS", "PLATFORM_MANAGE_OPERATORS")),
    PLATFORM_SUPPORT(Set.of("PLATFORM_VIEW_HOTELS"));

    private final Set<String> authorities;

    EPlatformRole(Set<String> authorities) { this.authorities = authorities; }

    public Set<String> authorities() { return authorities; }
}
