package rw.afriteck.pms.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateStaffProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String contactNumber,
        @Email String email,
        String position,
        @NotNull LocalDate hireDate,
        @NotNull UUID hotelBranchId
) {
}
