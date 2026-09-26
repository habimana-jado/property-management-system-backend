package rw.afriteck.pms.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.restaurant.model.MenuCategory;

import java.util.UUID;

public interface MenuCategoryRepo extends JpaRepository<MenuCategory, UUID> {
    Page<MenuCategory> findByRestaurantId(UUID restaurantId, Pageable pageable);
}
