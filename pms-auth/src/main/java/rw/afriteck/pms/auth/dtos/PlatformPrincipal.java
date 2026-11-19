package rw.afriteck.pms.auth.dtos;

import java.util.UUID;

public record PlatformPrincipal(UUID operatorId, String username) {}
