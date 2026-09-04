package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.model.Restaurant;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(target = "hotelBranchId", source = "hotelBranch.hotelBranchId")
    RestaurantResponse toResponse(Restaurant restaurant);
    Restaurant toEntity(CreateRestaurantRequest restaurantRequest);
}
