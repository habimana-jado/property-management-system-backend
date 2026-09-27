package rw.afriteck.pms.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.restaurant.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.restaurant.dtos.MenuMasterResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface MenuMasterService {
    MenuMasterResponse register(CreateMenuMasterRequest menuMasterRequest);

    MenuMasterResponse update(UUID id, CreateMenuMasterRequest menuMasterRequest);

    Page<MenuMasterResponse> findAll(Pageable pageable);

    MenuMasterResponse findOne(UUID menuMasterId);

    MenuMasterResponse updateUnitPrice(UUID menuMasterId, BigDecimal unitPrice);

    Page<MenuMasterResponse> findByRestaurantAndActive(UUID restaurantId, Pageable pageable);

    MenuMasterResponse activate(UUID menuMasterId);

    MenuMasterResponse deactivate(UUID menuMasterId);
}
