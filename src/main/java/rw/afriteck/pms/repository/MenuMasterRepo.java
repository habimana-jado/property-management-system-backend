package rw.afriteck.pms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.MenuMaster;

import java.util.UUID;

public interface MenuMasterRepo extends JpaRepository<MenuMaster, UUID> {
    Page<MenuMaster> findByMenuCategoryRestaurantId(UUID restaurantId, Pageable pageable);
}
