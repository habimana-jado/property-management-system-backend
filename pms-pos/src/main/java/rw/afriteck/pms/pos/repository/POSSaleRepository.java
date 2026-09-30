package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rw.afriteck.pms.pos.model.POSSale;

import java.util.List;
import java.util.UUID;

public interface POSSaleRepository extends JpaRepository<POSSale, UUID> {
    @Query("SELECT s.id FROM POSSale s WHERE s.registerSession.id = :sessionId")
    List<UUID> findIdsByRegisterSessionId(UUID sessionId);
}
