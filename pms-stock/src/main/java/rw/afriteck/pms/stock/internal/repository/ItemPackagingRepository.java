package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemPackagingRepository extends JpaRepository<ItemPackaging, UUID> {
    Optional<ItemPackaging> findByItemIdAndUnitId(UUID itemId, UUID unitId);
    boolean existsByItemId(UUID itemId);
    @EntityGraph(attributePaths = "unit")
    List<ItemPackaging> findByItemId(UUID itemId);
    @EntityGraph(attributePaths = "unit")
    List<ItemPackaging> findByItemIdIn(Collection<UUID> itemIds);

}
