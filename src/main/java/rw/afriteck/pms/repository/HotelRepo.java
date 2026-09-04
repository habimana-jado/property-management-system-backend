package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.Hotel;

import java.util.UUID;

public interface HotelRepo extends JpaRepository<Hotel, UUID> {}
