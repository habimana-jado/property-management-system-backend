package rw.afriteck.pms.platform.dtos;

import rw.afriteck.pms.property.dtos.HotelResponse;

import java.util.UUID;

public record OnboardHotelResponse(HotelResponse hotel, UUID adminStaffId, UUID adminUserId, String adminUsername) {}
