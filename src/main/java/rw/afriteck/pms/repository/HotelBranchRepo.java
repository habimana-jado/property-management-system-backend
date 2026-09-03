package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.HotelBranch;

import java.util.List;
import java.util.UUID;

public interface HotelBranchRepo extends JpaRepository<HotelBranch, UUID> {
    List<HotelBranch> findByHotelHotelId(UUID hotelId);
}
