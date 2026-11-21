package rw.afriteck.pms.property.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.property.model.Hotel;

import java.util.Optional;
import java.util.UUID;

public interface HotelRepo extends JpaRepository<Hotel, UUID> {

    boolean existsByIdAndStatus(UUID id, ERecordStatus status);

    boolean existsByHotelCode(String hotelCode);

    @Query("select h.hotelCode from Hotel h where h.id = :id")
    Optional<String> findHotelCodeById(@Param("id") UUID id);
}
