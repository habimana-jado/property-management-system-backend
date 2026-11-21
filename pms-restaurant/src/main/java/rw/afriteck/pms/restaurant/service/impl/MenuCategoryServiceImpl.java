package rw.afriteck.pms.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.restaurant.dtos.CreateMenuCategoryRequest;
import rw.afriteck.pms.restaurant.dtos.MenuCategoryResponse;
import rw.afriteck.pms.restaurant.mapper.MenuCategoryMapper;
import rw.afriteck.pms.restaurant.model.MenuCategory;
import rw.afriteck.pms.restaurant.model.Restaurant;
import rw.afriteck.pms.restaurant.repository.MenuCategoryRepo;
import rw.afriteck.pms.restaurant.repository.RestaurantRepo;
import rw.afriteck.pms.restaurant.service.MenuCategoryService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuCategoryServiceImpl implements MenuCategoryService {
    private final MenuCategoryRepo menuCategoryRepo;
    private final MenuCategoryMapper menuCategoryMapper;
    private final RestaurantRepo restaurantRepo;
    private final RestaurantBranchAccess branchAccess;

    @Override
    public MenuCategoryResponse register(CreateMenuCategoryRequest menuCategoryRequest) {
        Restaurant restaurant = restaurantRepo.findById(menuCategoryRequest.restaurantId())
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", menuCategoryRequest.restaurantId()));

        branchAccess.require(restaurant);

        MenuCategory menuCategory = menuCategoryMapper.toEntity(menuCategoryRequest);
        menuCategory.setRestaurant(restaurant);
        return menuCategoryMapper.toResponse(menuCategoryRepo.save(menuCategory));
    }

    @Override
    @Transactional
    public MenuCategoryResponse update(UUID id, CreateMenuCategoryRequest request) {
        MenuCategory category = menuCategoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu category", id));
        branchAccess.require(category.getRestaurant());

        Restaurant restaurant = restaurantRepo.findById(request.restaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", request.restaurantId()));
        branchAccess.require(restaurant);

        menuCategoryMapper.updateEntityFromRequest(request, category);
        category.setRestaurant(restaurant);
        return menuCategoryMapper.toResponse(category);
    }

    @Override
    public Page<MenuCategoryResponse> findAll(Pageable pageable) {
        return menuCategoryRepo.findAll(pageable)
                .map(menuCategoryMapper::toResponse);
    }

    @Override
    public Page<MenuCategoryResponse> findByHotelBranchId(UUID hotelBranchId, Pageable pageable) {
        return menuCategoryRepo.findByRestaurantHotelBranchId(hotelBranchId, pageable)
                .map(menuCategoryMapper::toResponse);
    }

    @Override
    public MenuCategoryResponse findOne(UUID id) {
        MenuCategory menuCategory = menuCategoryRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Menu Category", id));

        branchAccess.require(menuCategory.getRestaurant());

        return menuCategoryMapper.toResponse(menuCategory);
    }

    @Override
    public Page<MenuCategoryResponse> findByRestaurant(UUID restaurantId, Pageable pageable) {
        return menuCategoryRepo.findByRestaurantId(restaurantId, pageable)
                .map(menuCategoryMapper::toResponse);
    }
}
