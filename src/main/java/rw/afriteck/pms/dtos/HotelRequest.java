package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EStatus;

public record HotelRequest(
        String hotelName,
        String websiteUrl,
        String logoUrl,
        String slogan,
        EStatus status
) {
}
