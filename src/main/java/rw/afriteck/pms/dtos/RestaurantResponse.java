package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

public record RestaurantResponse(
        UUID restaurantId,
        String restaurantName,
        String tinNumber,
        ERecordStatus status,
        UUID hotelBranchId
) {}
