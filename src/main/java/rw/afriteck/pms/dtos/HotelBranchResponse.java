package rw.afriteck.pms.dtos;

import rw.afriteck.pms.enums.EStatus;

import java.util.UUID;

public record HotelBranchResponse(
        UUID hotelBranchId,
        String name,
        String location,
        String contactNumber1,
        String contactNumber2,
        EStatus status,
        UUID hotelId
) {}
