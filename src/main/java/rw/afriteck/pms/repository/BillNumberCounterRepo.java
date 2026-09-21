package rw.afriteck.pms.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.model.BillNumberCounter;

import java.util.Optional;
import java.util.UUID;

public interface BillNumberCounterRepo extends JpaRepository<BillNumberCounter, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BillNumberCounter c WHERE c.restaurantId = :restaurantId")
    Optional<BillNumberCounter> findByIdForUpdate(@Param("restaurantId") UUID restaurantId);
}
