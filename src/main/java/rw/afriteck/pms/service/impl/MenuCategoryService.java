package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.CreateMenuCategoryRequest;
import rw.afriteck.pms.dtos.MenuCategoryResponse;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.MenuCategoryMapper;
import rw.afriteck.pms.model.MenuCategory;
import rw.afriteck.pms.model.Restaurant;
import rw.afriteck.pms.repository.MenuCategoryRepo;
import rw.afriteck.pms.repository.RestaurantRepo;
import rw.afriteck.pms.service.IMenuCategoryService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuCategoryService implements IMenuCategoryService {
    private final MenuCategoryRepo menuCategoryRepo;
    private final MenuCategoryMapper menuCategoryMapper;
    private final RestaurantRepo restaurantRepo;

    @Override
    public MenuCategoryResponse register(CreateMenuCategoryRequest menuCategoryRequest) {
        Restaurant restaurant = restaurantRepo.findById(menuCategoryRequest.restaurantId())
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", menuCategoryRequest.restaurantId()));

        MenuCategory menuCategory = menuCategoryMapper.toEntity(menuCategoryRequest);
        menuCategory.setRestaurant(restaurant);
        return menuCategoryMapper.toResponse(menuCategoryRepo.save(menuCategory));
    }

    @Override
    public MenuCategoryResponse update(UUID id, CreateMenuCategoryRequest menuCategoryRequest) {
        Restaurant restaurant = restaurantRepo.findById(menuCategoryRequest.restaurantId())
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", menuCategoryRequest.restaurantId()));

        MenuCategory menuCategory = menuCategoryRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Menu Category", id));

        menuCategory.setRestaurant(restaurant);
        return menuCategoryMapper.toResponse(menuCategoryRepo.save(menuCategory));
    }

    @Override
    public Page<MenuCategoryResponse> findAll(Pageable pageable) {
        return menuCategoryRepo.findAll(pageable)
                .map(menuCategoryMapper::toResponse);
    }

    @Override
    public MenuCategoryResponse findOne(UUID id) {
        MenuCategory menuCategory = menuCategoryRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Menu Category", id));
        return menuCategoryMapper.toResponse(menuCategory);
    }

    @Override
    public Page<MenuCategoryResponse> findByRestaurant(UUID restaurantId, Pageable pageable) {
        return menuCategoryRepo.findByRestaurantId(restaurantId, pageable)
                .map(menuCategoryMapper::toResponse);
    }
}
