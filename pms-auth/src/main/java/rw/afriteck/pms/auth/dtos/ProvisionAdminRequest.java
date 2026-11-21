package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProvisionAdminRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]{2,49}$") String username,
        @NotBlank @Size(min = 8) String password) {}

