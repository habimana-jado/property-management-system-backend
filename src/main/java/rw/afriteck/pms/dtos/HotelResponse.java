package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

public record HotelResponse(
        UUID hotelId,
        String hotelName,
        String websiteUrl,
        String logoUrl,
        String slogan,
        ERecordStatus status
) {}
