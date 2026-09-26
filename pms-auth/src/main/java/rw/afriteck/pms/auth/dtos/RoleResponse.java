package rw.afriteck.pms.auth.dtos;

import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        boolean systemDefined,
        List<PermissionResponse> permissions
) {
}
