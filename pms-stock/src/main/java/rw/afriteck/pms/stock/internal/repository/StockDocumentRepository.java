package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.StockDocument;

import java.util.Optional;
import java.util.UUID;

public interface StockDocumentRepository extends JpaRepository<StockDocument, UUID> {
    @EntityGraph(attributePaths = {"store", "counterpartStore", "lines", "lines.item",
            "lines.item.baseUnit", "lines.enteredUnit"})
    Optional<StockDocument> findDetailedById(UUID id);
}
