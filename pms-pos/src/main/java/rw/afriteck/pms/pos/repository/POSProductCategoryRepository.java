package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.pos.model.POSProductCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface POSProductCategoryRepository extends JpaRepository<POSProductCategory, UUID> {

    Optional<POSProductCategory> findByHotelBranchIdAndCategoryNameIgnoreCase(UUID hotelBranchId, String categoryName);

    boolean existsByHotelBranchIdAndCategoryNameIgnoreCase(UUID hotelBranchId, String categoryName);

    List<POSProductCategory> findByHotelBranchId(UUID hotelBranchId);
}
