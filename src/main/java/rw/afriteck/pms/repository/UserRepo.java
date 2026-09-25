package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByStaffId(UUID staffId);
    List<User> findByStaffIdIn(List<UUID> staffIds);
}
