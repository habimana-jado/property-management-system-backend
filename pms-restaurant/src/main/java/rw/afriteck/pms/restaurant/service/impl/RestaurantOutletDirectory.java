package rw.afriteck.pms.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.restaurant.repository.RestaurantRepo;
import rw.afriteck.pms.stock.api.OutletDirectory;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class RestaurantOutletDirectory implements OutletDirectory {
    private final RestaurantRepo restaurants;

    @Override public EOutletType type() { return EOutletType.RESTAURANT; }
    @Override public Optional<UUID> branchIdOf(UUID outletId) {
        return restaurants.findById(outletId).map(r -> r.getHotelBranch().getId());
    }
}
