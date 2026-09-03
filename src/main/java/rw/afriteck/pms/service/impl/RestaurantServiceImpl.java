package rw.afriteck.pms.service.impl;

import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.RestaurantRequest;
import rw.afriteck.pms.enums.EStatus;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.model.Restaurant;
import rw.afriteck.pms.repository.RestaurantRepo;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IRestaurantService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RestaurantServiceImpl implements IRestaurantService {
    private final RestaurantRepo restaurantRepo;
    private final ObjectMapper objectMapper;
    private final IHotelBranchService hotelBranchService;

    public RestaurantServiceImpl(RestaurantRepo restaurantRepo, ObjectMapper objectMapper, IHotelBranchService hotelBranchService){
        this.restaurantRepo = restaurantRepo;
        this.objectMapper = objectMapper;
        this.hotelBranchService = hotelBranchService;
    }
    @Override
    public Restaurant registerRestaurant(RestaurantRequest restaurantRequest) {
        HotelBranch hotelBranch = this.hotelBranchService.findOne(restaurantRequest.hotelBranchId());
        Restaurant restaurant = this.objectMapper.convertValue(restaurantRequest, Restaurant.class);
        restaurant.setHotelBranch(hotelBranch);
        restaurant.setStatus(EStatus.ACTIVE);

        return this.restaurantRepo.save(restaurant);
    }

    @Override
    public List<Restaurant> findAll() {
        return this.restaurantRepo.findAll();
    }

    @Override
    public Restaurant findOne(UUID restaurantId) {
        Optional<Restaurant> restaurant = this.restaurantRepo.findById(restaurantId);
        return restaurant.orElse(null);
    }

    @Override
    public Restaurant activate(UUID restaurantId) {
        Optional<Restaurant> restaurant = this.restaurantRepo.findById(restaurantId);
        if(restaurant.isPresent()){
            Restaurant restaurant1 = restaurant.get();
            restaurant1.setStatus(EStatus.ACTIVE);
            return this.restaurantRepo.save(restaurant1);
        }else {
            return null;
        }
    }

    @Override
    public Restaurant deactivate(UUID restaurantId) {
        Optional<Restaurant> restaurant = this.restaurantRepo.findById(restaurantId);
        if(restaurant.isPresent()){
            Restaurant restaurant1 = restaurant.get();
            restaurant1.setStatus(EStatus.INACTIVE);
            return this.restaurantRepo.save(restaurant1);
        }else {
            return null;
        }
    }

    @Override
    public List<Restaurant> findByHotelBranch(UUID hotelBranchId) {
        return this.restaurantRepo.findByHotelBranchHotelBranchId(hotelBranchId);
    }
}
