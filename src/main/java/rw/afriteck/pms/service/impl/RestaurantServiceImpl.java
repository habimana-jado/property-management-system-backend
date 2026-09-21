package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.exception.BusinessRuleViolationException;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.RestaurantMapper;
import rw.afriteck.pms.model.BillNumberCounter;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.model.Restaurant;
import rw.afriteck.pms.repository.BillNumberCounterRepo;
import rw.afriteck.pms.repository.HotelBranchRepo;
import rw.afriteck.pms.repository.RestaurantRepo;
import rw.afriteck.pms.service.IRestaurantService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements IRestaurantService {
    private final RestaurantRepo restaurantRepo;
    private final RestaurantMapper restaurantMapper;
    private final HotelBranchRepo hotelBranchRepo;
    private final BillNumberCounterRepo billNumberCounterRepo;

    @Override
    @Transactional
    public RestaurantResponse create(CreateRestaurantRequest request) {
        HotelBranch hotelBranch = hotelBranchRepo.findById(request.hotelBranchId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", request.hotelBranchId()));
        Restaurant restaurant = this.restaurantMapper.toEntity(request);
        restaurant.setHotelBranch(hotelBranch);
        restaurant.setStatus(ERecordStatus.ACTIVE);

        Restaurant response = restaurantRepo.save(restaurant);

        BillNumberCounter counter = new BillNumberCounter();
        counter.setRestaurantId(response.getId());
        counter.setLastNumber(0L);
        billNumberCounterRepo.save(counter);

        return restaurantMapper.toResponse(response);
    }

    @Override
    @Transactional
    public RestaurantResponse update(UUID id, CreateRestaurantRequest restaurantRequest) {
        if (restaurantRequest.restaurantCode() != null) {
            throw new BusinessRuleViolationException("RESTAURANT_CODE_PATCH", "Restaurant code cannot be changed after creation");
        }

        HotelBranch hotelBranch = hotelBranchRepo.findById(restaurantRequest.hotelBranchId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", restaurantRequest.hotelBranchId()));

        Restaurant restaurant = restaurantRepo.findById(id)
                        .orElseThrow(()->new ResourceNotFoundException("Restaurant", id));

        restaurantMapper.updateEntityFromRequest(restaurantRequest, restaurant);

        restaurant.setHotelBranch(hotelBranch);
        return restaurantMapper.toResponse(restaurantRepo.save(restaurant));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> findAll(Pageable pageable) {
        return restaurantRepo.findAll(pageable)
                .map(restaurantMapper::toResponse);
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
        restaurant.setStatus(ERecordStatus.ACTIVE);
        return restaurantMapper.toResponse(restaurantRepo.save(restaurant));
    }

    @Override
    public RestaurantResponse deactivate(UUID restaurantId) {
        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", restaurantId));
        restaurant.setStatus(ERecordStatus.INACTIVE);
        return restaurantMapper.toResponse(restaurantRepo.save(restaurant));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantResponse> findByHotelBranchAndActive(UUID hotelBranchId, Pageable pageable) {
        return restaurantRepo.findByHotelBranchIdAndStatus(hotelBranchId, ERecordStatus.ACTIVE, pageable)
                .map(restaurantMapper::toResponse);
    }
}
