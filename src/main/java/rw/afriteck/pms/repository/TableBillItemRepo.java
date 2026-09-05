package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.model.TableBillItem;

import java.math.BigDecimal;
import java.util.UUID;

public interface TableBillItemRepo extends JpaRepository<TableBillItem, UUID> {

    @Query("SELECT COALESCE(SUM(i.lineTotal), 0) FROM TableBillItem i WHERE i.tableBill.id = :billId")
    BigDecimal sumLineTotalsByBillId(@Param("billId") UUID billId);
}
