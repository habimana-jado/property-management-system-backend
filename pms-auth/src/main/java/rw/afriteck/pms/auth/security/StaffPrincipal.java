package rw.afriteck.pms.auth.security;

import java.util.UUID;

public record StaffPrincipal(UUID userId, UUID staffId, String username, UUID hotelBranchId) {
}
