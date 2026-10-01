package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.Store;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}
