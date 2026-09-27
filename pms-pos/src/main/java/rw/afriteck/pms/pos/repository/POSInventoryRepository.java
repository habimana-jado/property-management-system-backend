package rw.afriteck.pms.pos.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import rw.afriteck.pms.pos.model.POSInventory;

import java.util.Optional;
import java.util.UUID;

public interface POSInventoryRepository extends JpaRepository<POSInventory, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from POSInventory i where i.product.id = :productId and i.hotelBranchId = :hotelBranchId")
    Optional<POSInventory> findForUpdate(UUID productId, UUID hotelBranchId);

    Optional<POSInventory> findByProductIdAndHotelBranchId(UUID productId, UUID hotelBranchId);
}