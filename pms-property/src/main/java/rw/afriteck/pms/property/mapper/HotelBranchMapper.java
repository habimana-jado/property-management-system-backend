package rw.afriteck.pms.property.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.property.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.property.dtos.HotelBranchResponse;
import rw.afriteck.pms.property.model.HotelBranch;

@Mapper(componentModel = "spring")
public interface HotelBranchMapper {

    HotelBranchResponse toResponse(HotelBranch hotelBranch);   // hotelId now maps by name

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "status", ignore = true)
    HotelBranch toEntity(CreateHotelBranchRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromRequest(CreateHotelBranchRequest request, @MappingTarget HotelBranch entity);
}
