package rw.afriteck.pms.credit.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.credit.entity.CreditCustomer;

import java.util.Optional;
import java.util.UUID;

public interface CreditCustomerRepository extends JpaRepository<CreditCustomer, UUID> {
    Optional<CreditCustomer> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreditCustomer c WHERE c.id = :id")
    Optional<CreditCustomer> findByIdForUpdate(@Param("id") UUID id);
}
