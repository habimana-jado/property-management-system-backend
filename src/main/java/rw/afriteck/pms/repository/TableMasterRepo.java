package rw.afriteck.pms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.model.TableMaster;

import java.util.UUID;

public interface TableMasterRepo extends JpaRepository<TableMaster, UUID> {
    Page<TableMaster> findByRestaurantIdAndRecordStatus(UUID restaurantId, ERecordStatus recordStatus, Pageable pageable);
}
