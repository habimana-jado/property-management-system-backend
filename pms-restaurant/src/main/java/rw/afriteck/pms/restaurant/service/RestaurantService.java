package rw.afriteck.pms.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.restaurant.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.restaurant.dtos.MenuItemsByCategoryResponse;
import rw.afriteck.pms.restaurant.dtos.RestaurantResponse;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {
    RestaurantResponse create(CreateRestaurantRequest restaurantRequest);

    RestaurantResponse update(UUID id, CreateRestaurantRequest restaurantRequest);

    Page<RestaurantResponse> findAll(Pageable pageable);

    RestaurantResponse findOne(UUID restaurantId);

    List<MenuItemsByCategoryResponse> searchMenuItemsGrouped(UUID restaurantId, String keyword);

    RestaurantResponse activate(UUID restaurantId);

    RestaurantResponse deactivate(UUID restaurantId);

    Page<RestaurantResponse> findByHotelBranchAndActive(UUID hotelBranchId, Pageable pageable);

}
