package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank String name,
        String description,
        @NotEmpty Set<String> permissionCodes
) {
}
