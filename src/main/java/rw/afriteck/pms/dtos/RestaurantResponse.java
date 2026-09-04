package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EStatus;

import java.util.UUID;

public record RestaurantResponse(
        UUID restaurantId,
        String restaurantName,
        String tinNumber,
        EStatus status,
        UUID hotelBranchId
) {}
