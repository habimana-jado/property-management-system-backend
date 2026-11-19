package rw.afriteck.pms.auth.dtos;

import rw.afriteck.pms.auth.enums.EPlatformRole;

import java.time.Instant;
import java.util.UUID;

public record PlatformOperatorResponse(
        UUID id, String username, String fullName, EPlatformRole role, boolean enabled, Instant lastLoginAt) {}
