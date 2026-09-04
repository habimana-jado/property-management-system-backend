package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import rw.afriteck.pms.dtos.CreateHotelRequest;
import rw.afriteck.pms.dtos.HotelResponse;
import rw.afriteck.pms.model.Hotel;

@Mapper(componentModel = "spring")
public interface HotelMapper {
    HotelResponse toResponse(Hotel hotel);
    Hotel toEntity(CreateHotelRequest hotelRequest);
    Hotel toHotelEntity(HotelResponse hotelResponse);
}
