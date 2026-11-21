package rw.afriteck.pms.property.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateHotelRequest(
    @NotBlank
    String hotelName,

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "Hotel code must be 2-10 uppercase letters or digits")
    String hotelCode,

    @NotBlank
    @Email
    String email,

    String websiteUrl,
    String logoUrl,
    String slogan
) {
}
