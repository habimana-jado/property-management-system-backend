package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.StockCategory;

import java.util.UUID;

public interface StockCategoryRepository extends JpaRepository<StockCategory, UUID> {
}
