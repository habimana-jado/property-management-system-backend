package rw.afriteck.pms.property.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateHotelRequest(
    @NotBlank String hotelName,
    @NotBlank @Email String email,
    String websiteUrl,
    String logoUrl,
    String slogan
) {
}
