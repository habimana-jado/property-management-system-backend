package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProvisionAdminRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        @NotBlank @Size(min = 4, max = 100) String username,
        @NotBlank @Size(min = 8) String password) {}

