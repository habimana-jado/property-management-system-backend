package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.Permission;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionRepo extends JpaRepository<Permission, UUID> {
    List<Permission> findByCodeIn(Set<String> codes);
}
