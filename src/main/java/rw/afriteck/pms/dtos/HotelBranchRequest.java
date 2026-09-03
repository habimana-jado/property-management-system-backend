package rw.afriteck.pms.dtos;

import java.util.UUID;

public record HotelBranchRequest(
    String name,
    String location,
    String contactNumber1,
    String contactNumber2,
    UUID hotelId
) {
}
