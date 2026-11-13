package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.internal.model.StoreAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreAssignmentRepository extends JpaRepository<StoreAssignment, UUID> {

    List<StoreAssignment> findByOutletTypeAndOutletIdAndActiveTrue(EOutletType type, UUID outletId);

    Optional<StoreAssignment> findByStoreIdAndOutletTypeAndOutletId(UUID storeId, EOutletType type, UUID outletId);

    boolean existsByStoreIdAndActiveTrue(UUID storeId);

    List<StoreAssignment> findByStoreId(UUID storeId);

    List<StoreAssignment> findByOutletTypeAndOutletId(EOutletType type, UUID outletId);
}
