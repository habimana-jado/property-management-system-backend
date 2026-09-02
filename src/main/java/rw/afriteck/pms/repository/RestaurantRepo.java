package rw.afriteck.pms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.afriteck.pms.model.Restaurant;

import java.util.UUID;

public interface RestaurantRepo extends JpaRepository<Restaurant, UUID> {
}
