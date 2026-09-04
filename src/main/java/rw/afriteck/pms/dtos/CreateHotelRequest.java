package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelRequest(
    @NotBlank String hotelName,
    String email,
    String websiteUrl,
    String logoUrl,
    String slogan
) {
}
