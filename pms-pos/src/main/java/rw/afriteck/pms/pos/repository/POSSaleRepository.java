package rw.afriteck.pms.pos.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.pos.model.POSSale;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface POSSaleRepository extends JpaRepository<POSSale, UUID> {

    @Query("SELECT s.id FROM POSSale s WHERE s.registerSession.id = :sessionId")
    List<UUID> findIdsByRegisterSessionId(UUID sessionId);

    Optional<POSSale> findByIdAndHotelBranchId(UUID id, UUID hotelBranchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM POSSale s WHERE s.id = :id")
    Optional<POSSale> findByIdForUpdate(@Param("id") UUID id);
}
