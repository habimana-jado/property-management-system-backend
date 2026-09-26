package rw.afriteck.pms.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.auth.model.Permission;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionRepo extends JpaRepository<Permission, UUID> {
    List<Permission> findByCodeIn(Set<String> codes);
}
