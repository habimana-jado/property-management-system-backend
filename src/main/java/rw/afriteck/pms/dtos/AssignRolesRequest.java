package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public record AssignRolesRequest(@NotEmpty Set<UUID> roleIds) {
}
