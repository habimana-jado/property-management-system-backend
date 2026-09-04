package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRestaurantRequest(
    @NotBlank String restaurantName,
    String tinNumber,
    @NotNull UUID hotelBranchId
) {
}
