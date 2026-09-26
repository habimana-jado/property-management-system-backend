package rw.afriteck.pms.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.payment.model.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentRepo extends JpaRepository<Payment, UUID> {

//    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.source_reference_id = :billId AND p.status = 'COMPLETED'")
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal sumActiveAmountByBillId(@Param("billId") UUID billId);
}
