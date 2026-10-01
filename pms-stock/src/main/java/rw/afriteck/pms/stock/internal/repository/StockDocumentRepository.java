package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.StockDocument;

import java.util.UUID;

public interface StockDocumentRepository extends JpaRepository<StockDocument, UUID> {
}
