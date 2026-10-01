package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.StockDocumentLine;

import java.util.UUID;

public interface StockDocumentLineRepository extends JpaRepository<StockDocumentLine, UUID> {
}
