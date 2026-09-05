package rw.afriteck.pms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.dtos.CreateMenuCategoryRequest;
import rw.afriteck.pms.dtos.MenuCategoryResponse;

import java.util.UUID;

public interface IMenuCategoryService {
    MenuCategoryResponse register(CreateMenuCategoryRequest menuCategoryRequest);

    MenuCategoryResponse update(UUID id, CreateMenuCategoryRequest menuCategoryRequest);

    Page<MenuCategoryResponse> findAll(Pageable pageable);

    MenuCategoryResponse findOne(UUID id);

    Page<MenuCategoryResponse> findByRestaurant(UUID restaurantId, Pageable pageable);
}
