package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.pos.model.POSStockMovement;

import java.util.UUID;

public interface POSStockMovementRepository extends JpaRepository<POSStockMovement, UUID> {
}
