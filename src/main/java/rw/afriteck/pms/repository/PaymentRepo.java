package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.model.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentRepo extends JpaRepository<Payment, UUID> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tableBill.id = :billId AND p.status = 'COMPLETED'")
    BigDecimal sumActiveAmountByBillId(@Param("billId") UUID billId);
}
