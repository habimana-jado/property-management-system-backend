package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.ERecordStatus;

import java.util.UUID;

public record HotelResponse(
        UUID id,
        String hotelName,
        String email,
        String websiteUrl,
        String logoUrl,
        String slogan,
        ERecordStatus status
) {}
