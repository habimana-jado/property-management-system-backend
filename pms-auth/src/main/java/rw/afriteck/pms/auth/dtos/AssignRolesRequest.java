package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public record AssignRolesRequest(@NotEmpty Set<UUID> roleIds) {
}
