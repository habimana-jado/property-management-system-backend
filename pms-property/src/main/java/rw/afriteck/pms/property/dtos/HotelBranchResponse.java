package rw.afriteck.pms.property.dtos;

import rw.afriteck.pms.common.enums.ERecordStatus;

import java.util.UUID;

public record HotelBranchResponse(
        UUID id,
        String name,
        String location,
        String contactNumber1,
        String contactNumber2,
        ERecordStatus status,
        UUID hotelId
) {}
