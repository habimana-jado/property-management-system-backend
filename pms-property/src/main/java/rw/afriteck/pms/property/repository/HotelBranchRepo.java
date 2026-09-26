package rw.afriteck.pms.property.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.property.model.HotelBranch;

import java.util.UUID;

public interface HotelBranchRepo extends JpaRepository<HotelBranch, UUID> {
    Page<HotelBranch> findByHotelIdAndStatus(UUID hotelId, ERecordStatus status, Pageable pageable);
}
