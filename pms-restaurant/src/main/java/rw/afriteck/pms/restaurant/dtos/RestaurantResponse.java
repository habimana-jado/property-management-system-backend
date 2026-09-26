package rw.afriteck.pms.restaurant.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String restaurantName,
        String tinNumber,
        String restaurantCode,
        ERecordStatus status,
        UUID hotelBranchId
) {}
