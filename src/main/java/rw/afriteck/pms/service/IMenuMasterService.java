package rw.afriteck.pms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.dtos.MenuMasterResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface IMenuMasterService {
    MenuMasterResponse register(CreateMenuMasterRequest menuMasterRequest);
    Page<MenuMasterResponse> findAll(Pageable pageable);
    MenuMasterResponse findOne(UUID menuMasterId);
    MenuMasterResponse updateUnitPrice(UUID menuMasterId, BigDecimal unitPrice);
    Page<MenuMasterResponse> findByRestaurantAndActive(UUID restaurantId, Pageable pageable);
    MenuMasterResponse activate(UUID menuMasterId);
    MenuMasterResponse deactivate(UUID menuMasterId);
}
