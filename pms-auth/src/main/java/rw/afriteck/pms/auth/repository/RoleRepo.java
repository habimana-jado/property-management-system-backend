package rw.afriteck.pms.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.auth.model.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepo extends JpaRepository<Role, UUID> {
    Optional<Role> findByIdAndHotelId(UUID id, UUID hotelId);

    List<Role> findByHotelId(UUID hotelId);

    List<Role> findAllByIdInAndHotelId(Collection<UUID> ids, UUID hotelId);

    boolean existsByHotelIdAndName(UUID hotelId, String name);
}

