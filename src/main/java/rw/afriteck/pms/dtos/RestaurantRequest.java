package rw.afriteck.pms.dtos;

import java.util.UUID;

public record RestaurantRequest(
    String restaurantName,
    String tinNumber,
    UUID hotelBranchId
) {
}
