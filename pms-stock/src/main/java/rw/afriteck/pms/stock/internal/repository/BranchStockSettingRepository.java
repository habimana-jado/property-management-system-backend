package rw.afriteck.pms.stock.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.stock.internal.model.BranchStockSetting;

import java.util.Optional;
import java.util.UUID;

public interface BranchStockSettingRepository extends JpaRepository<BranchStockSetting, UUID> {
    Optional<BranchStockSetting> findByBranchId(UUID branchId);
}
