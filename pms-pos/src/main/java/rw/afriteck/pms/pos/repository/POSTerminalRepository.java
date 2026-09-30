package rw.afriteck.pms.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.pos.model.POSTerminal;

import java.util.List;
import java.util.UUID;

public interface POSTerminalRepository extends JpaRepository<POSTerminal, UUID> {
    boolean existsByHotelBranchIdAndCode(UUID hotelBranchId, String code);

    List<POSTerminal> findByHotelBranchId(UUID hotelBranchId);
}
