package rw.afriteck.pms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateHotelBranchRequest(
    @NotBlank String name,
    @NotBlank String location,
    String contactNumber1,
    String contactNumber2,
    @NotNull UUID hotelId
) {
}
