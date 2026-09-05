package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.model.HotelBranch;

@Mapper(componentModel = "spring")
public interface HotelBranchMapper {

    @Mapping(target = "hotelId", source = "hotel.id")
    HotelBranchResponse toResponse(HotelBranch hotelBranch);

    HotelBranch toEntity(CreateHotelBranchRequest hotelBranchRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    void updateEntityFromRequest(CreateHotelBranchRequest request, @MappingTarget HotelBranch entity);

}
