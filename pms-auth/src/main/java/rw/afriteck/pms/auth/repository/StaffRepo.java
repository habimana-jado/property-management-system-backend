package rw.afriteck.pms.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.auth.model.Staff;

import java.util.Optional;
import java.util.UUID;

public interface StaffRepo extends JpaRepository<Staff, UUID> {
    Page<Staff> findByHotelBranchId(UUID hotelBranchId, Pageable pageable);
    Optional<Staff> findByEmail(String email);
}
