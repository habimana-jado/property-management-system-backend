package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateRestaurantRequest(
    @NotBlank String restaurantName,
    String tinNumber,
    @Pattern(regexp = "^[A-Z0-9]{2,6}$", message = "Code must be 2-6 uppercase alphanumeric characters") String restaurantCode,
    @NotNull UUID hotelBranchId
) {
}
