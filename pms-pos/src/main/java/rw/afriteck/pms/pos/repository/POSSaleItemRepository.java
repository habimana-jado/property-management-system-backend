package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.pos.model.POSSaleItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface POSSaleItemRepository extends JpaRepository<POSSaleItem, UUID> {

    Optional<POSSaleItem> findBySaleIdAndProductId(UUID saleId, UUID productId);

    List<POSSaleItem> findBySaleId(UUID saleId);
}
