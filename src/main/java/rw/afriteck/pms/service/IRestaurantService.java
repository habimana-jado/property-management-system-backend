package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.model.Restaurant;

import java.util.List;
import java.util.UUID;

public interface IRestaurantService {
    public RestaurantResponse create(CreateRestaurantRequest restaurantRequest);
    public List<RestaurantResponse> findAll();
    public RestaurantResponse findOne(UUID restaurantId);
    public RestaurantResponse activate(UUID restaurantId);
    public RestaurantResponse deactivate(UUID restaurantId);
    public List<RestaurantResponse> findByHotelBranch(UUID hotelBranchId);
}
