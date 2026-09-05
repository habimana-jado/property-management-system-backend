package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.enums.EBillStatus;
import rw.afriteck.pms.model.TableBill;

import java.util.Optional;
import java.util.UUID;

public interface TableBillRepo extends JpaRepository<TableBill, UUID> {

    Optional<TableBill> findByTableMasterIdAndStatus(UUID tableId, EBillStatus status);

}
