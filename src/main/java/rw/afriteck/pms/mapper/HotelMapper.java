package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.dtos.CreateHotelRequest;
import rw.afriteck.pms.dtos.HotelResponse;
import rw.afriteck.pms.model.Hotel;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    HotelResponse toResponse(Hotel hotel);

    Hotel toEntity(CreateHotelRequest hotelRequest);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(CreateHotelRequest request, @MappingTarget Hotel entity);
}
