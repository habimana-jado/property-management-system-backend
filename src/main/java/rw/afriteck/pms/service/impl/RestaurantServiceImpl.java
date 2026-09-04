package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.enums.EStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.RestaurantMapper;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.model.Restaurant;
import rw.afriteck.pms.repository.HotelBranchRepo;
import rw.afriteck.pms.repository.RestaurantRepo;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IRestaurantService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements IRestaurantService {
    private final RestaurantRepo restaurantRepo;
    private final RestaurantMapper restaurantMapper;
    private final HotelBranchRepo hotelBranchRepo;

    @Override
    public RestaurantResponse create(CreateRestaurantRequest request) {
        HotelBranch hotelBranch = hotelBranchRepo.findById(request.hotelBranchId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", request.hotelBranchId()));
        Restaurant restaurant = this.restaurantMapper.toEntity(request);
        restaurant.setHotelBranch(hotelBranch);
        restaurant.setStatus(EStatus.ACTIVE);

        return restaurantMapper.toResponse(restaurantRepo.save(restaurant));
    }

    @Override
    public List<RestaurantResponse> findAll() {
        return restaurantRepo.findAll()
                .stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }

    @Override
    public RestaurantResponse findOne(UUID restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", restaurantId));
        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    public RestaurantResponse activate(UUID restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", restaurantId));
        restaurant.setStatus(EStatus.ACTIVE);
        return restaurantMapper.toResponse(restaurantRepo.save(restaurant));
    }

    @Override
    public RestaurantResponse deactivate(UUID restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", restaurantId));
        restaurant.setStatus(EStatus.INACTIVE);
        return restaurantMapper.toResponse(restaurantRepo.save(restaurant));
    }

    @Override
    public List<RestaurantResponse> findByHotelBranch(UUID hotelBranchId) {
        return restaurantRepo.findByHotelBranchHotelBranchId(hotelBranchId)
                .stream()
                .map(restaurantMapper::toResponse)
                .toList();
    }
}
