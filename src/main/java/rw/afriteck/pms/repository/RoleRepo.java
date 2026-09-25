package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepo extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
