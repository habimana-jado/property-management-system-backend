package rw.afriteck.pms.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.model.PlatformOperator;

import java.util.Optional;
import java.util.UUID;

public interface PlatformOperatorRepo extends JpaRepository<PlatformOperator, UUID> {
    Optional<PlatformOperator> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByRoleAndEnabledTrue(EPlatformRole role);
}
