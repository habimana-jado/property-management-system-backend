package rw.afriteck.pms.dtos;

import java.util.UUID;

public record PermissionResponse(UUID id, String code, String description) {
}
