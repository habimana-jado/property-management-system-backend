package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.internal.model.Store;

import java.util.List;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    boolean existsByBranchIdAndCode(UUID branchId, String code);

    boolean existsByBranchIdAndStoreType(UUID branchId, EStoreType type);

    List<Store> findByBranchIdOrderByName(UUID branchId);
}
