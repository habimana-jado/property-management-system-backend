package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.afriteck.pms.auth.enums.EPlatformRole;

public record CreatePlatformOperatorRequest(
        @NotBlank @Size(min = 4, max = 100) String username,
        @NotBlank String fullName,
        @NotBlank @Size(min = 12) String password,
        @NotNull EPlatformRole role) {}

