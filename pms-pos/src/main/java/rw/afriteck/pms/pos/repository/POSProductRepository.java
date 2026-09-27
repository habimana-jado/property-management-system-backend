package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.pos.model.POSProduct;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface POSProductRepository extends JpaRepository<POSProduct, UUID> {

    Optional<POSProduct> findByHotelBranchIdAndSku(UUID hotelBranchId, String sku);

    boolean existsByHotelBranchIdAndSku(UUID hotelBranchId, String sku);

    List<POSProduct> findByHotelBranchId(UUID hotelBranchId);

    List<POSProduct> findByHotelBranchIdAndCategoryId(UUID hotelBranchId, UUID categoryId);
}
