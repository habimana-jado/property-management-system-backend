package rw.afriteck.pms.pos.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import rw.afriteck.pms.pos.model.POSSaleNumberCounter;

import java.util.Optional;
import java.util.UUID;

public interface POSSaleNumberCounterRepository  extends JpaRepository<POSSaleNumberCounter, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from POSSaleNumberCounter c where c.hotelBranchId = :hotelBranchId")
    Optional<POSSaleNumberCounter> findForUpdate(UUID hotelBranchId);

}
