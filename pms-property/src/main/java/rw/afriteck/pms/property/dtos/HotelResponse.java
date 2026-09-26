package rw.afriteck.pms.property.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;

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
