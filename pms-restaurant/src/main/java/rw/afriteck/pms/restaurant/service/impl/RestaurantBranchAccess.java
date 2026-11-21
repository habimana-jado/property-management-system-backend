package rw.afriteck.pms.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.security.BranchAccessGuard;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.restaurant.model.Restaurant;
import rw.afriteck.pms.restaurant.model.TableBill;
import rw.afriteck.pms.restaurant.model.TableMaster;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class RestaurantBranchAccess {

    private final BranchAccessGuard guard;

    void requireBranch(UUID branchId) { guard.requireAccessTo(branchId); }

    void require(Restaurant restaurant) {
        HotelBranch branch = restaurant.getHotelBranch();
        guard.requireAccessTo(branch == null ? null : branch.getId());
    }

    void require(TableMaster table) { require(table.getRestaurant()); }

    void require(TableBill bill) { require(bill.getTableMaster()); }

    Optional<UUID> restrictedBranchId() { return guard.restrictedBranchId(); }
}
