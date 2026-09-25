package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateRolePermissionsRequest(@NotEmpty Set<String> permissionCodes) {
}
