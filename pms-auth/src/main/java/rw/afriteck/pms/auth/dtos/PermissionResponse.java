package rw.afriteck.pms.auth.dtos;

import java.util.UUID;

public record PermissionResponse(UUID id, String code, String description) {
}
