package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.pos.enums.ERegisterSessionStatus;
import rw.afriteck.pms.pos.model.POSRegisterSession;

import java.util.Optional;
import java.util.UUID;

public interface POSRegisterSessionRepository extends JpaRepository<POSRegisterSession, UUID> {

    Optional<POSRegisterSession> findByTerminalIdAndStatus(UUID terminalId, ERegisterSessionStatus status);

    boolean existsByTerminalIdAndStatus(UUID terminalId, ERegisterSessionStatus status);
}
