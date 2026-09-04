package rw.afriteck.pms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.model.Restaurant;

import java.util.UUID;

public interface RestaurantRepo extends JpaRepository<Restaurant, UUID> {
    Page<Restaurant> findByHotelBranchIdAndStatus(UUID hotelBranchId, ERecordStatus status, Pageable pageable);
}
