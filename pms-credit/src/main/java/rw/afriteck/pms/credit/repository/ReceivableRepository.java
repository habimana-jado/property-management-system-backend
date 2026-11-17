package rw.afriteck.pms.credit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceivableRepository extends JpaRepository<Receivable, UUID> {

    boolean existsBySourceTypeAndSourceId(EPayableType sourceType, UUID sourceId);

    @Query("""
        SELECT COALESCE(SUM(r.originalAmount - r.amountPaid), 0) FROM Receivable r
        WHERE r.customer.id = :customerId AND r.status IN :statuses
        """)
    BigDecimal sumOutstandingByCustomer(@Param("customerId") UUID customerId,
                                        @Param("statuses") List<EReceivableStatus> statuses);

    List<Receivable> findByCustomerIdAndStatusInOrderByIssuedAtAsc(UUID customerId, List<EReceivableStatus> statuses);

    @Query("SELECT r.customer.id FROM Receivable r WHERE r.id = :id")
    Optional<UUID> findCustomerIdById(@Param("id") UUID id);

    @Query("""
    SELECT r FROM Receivable r JOIN FETCH r.customer c
    WHERE c.hotelId = :hotelId AND r.status IN :statuses
    """)
    List<Receivable> findActiveByHotelWithCustomer(@Param("hotelId") UUID hotelId,
                                                   @Param("statuses") List<EReceivableStatus> statuses);
}
