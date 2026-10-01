package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.StockItem;

import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
}
