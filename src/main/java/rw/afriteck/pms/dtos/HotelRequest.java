package rw.afriteck.pms.dtos;

public record HotelRequest(
    String hotelName,
    String websiteUrl,
    String logoUrl,
    String slogan
) {
}
