package rw.afriteck.pms.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.restaurant.enums.EBillStatus;
import rw.afriteck.pms.restaurant.model.TableBill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableBillRepo extends JpaRepository<TableBill, UUID> {

    Optional<TableBill> findByTableMasterIdAndBillStatus(UUID tableId, EBillStatus status);

    @Query("SELECT tb FROM TableBill tb WHERE tb.tableMaster.id = :tableId AND tb.billStatus IN :statuses")
    Optional<TableBill> findActiveBillByTableId(
            @Param("tableId") UUID tableId,
            @Param("statuses") List<EBillStatus> statuses
    );
}
