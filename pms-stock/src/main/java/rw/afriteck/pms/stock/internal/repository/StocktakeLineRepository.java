package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.StocktakeLine;

import java.util.UUID;

public interface StocktakeLineRepository extends JpaRepository<StocktakeLine, UUID> {
}
