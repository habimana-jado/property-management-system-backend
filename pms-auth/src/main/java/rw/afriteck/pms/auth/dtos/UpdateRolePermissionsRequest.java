package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolePermissionsRequest(@NotEmpty Set<String> permissionCodes) {
}
