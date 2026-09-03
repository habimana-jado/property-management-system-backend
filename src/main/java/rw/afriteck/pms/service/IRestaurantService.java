package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.RestaurantRequest;
import rw.afriteck.pms.model.Restaurant;

import java.util.List;
import java.util.UUID;

public interface IRestaurantService {
    public Restaurant registerRestaurant(RestaurantRequest restaurantRequest);
    public List<Restaurant> findAll();
    public Restaurant findOne(UUID restaurantId);
    public Restaurant activate(UUID restaurantId);
    public Restaurant deactivate(UUID restaurantId);
    public List<Restaurant> findByHotelBranch(UUID hotelBranchId);
}
