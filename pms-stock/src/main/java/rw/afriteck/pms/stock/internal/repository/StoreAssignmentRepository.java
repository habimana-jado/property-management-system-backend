package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.internal.model.StoreAssignment;

import java.util.List;
import java.util.UUID;

public interface StoreAssignmentRepository extends JpaRepository<StoreAssignment, UUID> {
    List<StoreAssignment> findByOutletTypeAndOutletIdAndActiveTrue(EOutletType type, UUID outletId);
}
