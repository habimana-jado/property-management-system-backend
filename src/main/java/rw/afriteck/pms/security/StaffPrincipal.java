package rw.afriteck.pms.security;

import java.util.UUID;

/**
 * Lightweight "current user" view built straight from JWT claims on every request -
 * no DB hit needed to know who's calling. Inject via @AuthenticationPrincipal in
 * controllers, or read via SecurityContextHolder in @tableSecurity-style beans.
 */
public record StaffPrincipal(UUID staffId, String username, UUID hotelBranchId) {
}
