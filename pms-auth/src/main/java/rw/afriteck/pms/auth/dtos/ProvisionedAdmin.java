package rw.afriteck.pms.auth.dtos;

import java.util.UUID;

public record ProvisionedAdmin(UUID staffId, UUID userId, UUID roleId, String username) {}
