package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.UnitOfMeasure;

import java.util.UUID;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {
}
