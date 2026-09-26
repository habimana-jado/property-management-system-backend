package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record StaffRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String contactNumber,
        @Email String email,
        String position,
        LocalDate hireDate,     // defaults to today if omitted
        @NotNull UUID hotelBranchId,

        @NotBlank @Size(min = 4, max = 100) String username,
        @NotBlank @Size(min = 8) String password,
        @NotEmpty Set<UUID> roleIds
) {
}
