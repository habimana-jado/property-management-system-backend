package rw.afriteck.pms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.model.MenuMaster;

import java.util.List;
import java.util.UUID;

public interface MenuMasterRepo extends JpaRepository<MenuMaster, UUID> {
    Page<MenuMaster> findByMenuCategoryRestaurantId(UUID restaurantId, Pageable pageable);

    @Query("""
        SELECT m FROM MenuMaster m
        JOIN FETCH m.menuCategory mc
        WHERE mc.restaurant.id = :restaurantId AND m.status = 'ACTIVE'
        ORDER BY m.menuItemName ASC
    """)
    List<MenuMaster> findAllActiveByRestaurant(@Param("restaurantId") UUID restaurantId);

    @Query("""
        SELECT m FROM MenuMaster m
        JOIN FETCH m.menuCategory mc
        WHERE mc.restaurant.id = :restaurantId AND m.status = 'ACTIVE'
        AND m.menuItemName ILIKE CONCAT('%', :keyword, '%')
        ORDER BY m.menuItemName ASC
        """)
    List<MenuMaster> searchActiveByRestaurantAndKeyword(
            @Param("restaurantId") UUID restaurantId,
            @Param("keyword") String keyword
    );
}
