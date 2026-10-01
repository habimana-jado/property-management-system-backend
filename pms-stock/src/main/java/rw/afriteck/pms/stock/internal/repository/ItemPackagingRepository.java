package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;

import java.util.Optional;
import java.util.UUID;

public interface ItemPackagingRepository extends JpaRepository<ItemPackaging, UUID> {
    Optional<ItemPackaging> findByItemIdAndUnitId(UUID itemId, UUID unitId);
}
