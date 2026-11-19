package rw.afriteck.pms.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.property.model.Hotel;

import java.util.UUID;

public interface HotelRepo extends JpaRepository<Hotel, UUID> {

    boolean existsByIdAndStatus(UUID id, ERecordStatus status);
}
