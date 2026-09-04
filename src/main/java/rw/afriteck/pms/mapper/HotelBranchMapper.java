package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.model.HotelBranch;

@Mapper(componentModel = "spring")
public interface HotelBranchMapper {

    @Mapping(target = "hotelId", source = "hotel.hotelId")
    HotelBranchResponse toResponse(HotelBranch hotelBranch);
    HotelBranch toEntity(CreateHotelBranchRequest hotelBranchRequest);
}
