package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EStatus;

import java.util.UUID;

public record HotelResponse(
        UUID hotelId,
        String hotelName,
        String websiteUrl,
        String logoUrl,
        String slogan,
        EStatus status
) {}
