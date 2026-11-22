package rw.afriteck.pms.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.restaurant.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.restaurant.dtos.MenuMasterResponse;
import rw.afriteck.pms.restaurant.dtos.UpdatePriceRequest;
import rw.afriteck.pms.restaurant.security.RestaurantPermissions;
import rw.afriteck.pms.restaurant.service.MenuMasterService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/restaurant/menu-items")
@RequiredArgsConstructor
public class MenuMasterController {
    private final MenuMasterService menuMasterService;

    @PostMapping
    @PreAuthorize(RestaurantPermissions.Has.SETUP_MANAGE)
    public ResponseEntity<MenuMasterResponse> register(@Valid @RequestBody CreateMenuMasterRequest menuMasterRequest){
        MenuMasterResponse menuMasterResponse = menuMasterService.register(menuMasterRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(menuMasterResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(menuMasterResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize(RestaurantPermissions.Has.SETUP_MANAGE)
    public ResponseEntity<MenuMasterResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody CreateMenuMasterRequest menuMasterRequest){
        MenuMasterResponse menuMasterResponse = menuMasterService.update(id, menuMasterRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(menuMasterResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(menuMasterResponse);
    }

    @GetMapping
    @PreAuthorize(RestaurantPermissions.Has.SETUP_VIEW)
    public ResponseEntity<Page<MenuMasterResponse>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(menuMasterService.findAll(pageable));
    }

    @GetMapping
    @PreAuthorize(RestaurantPermissions.Has.SETUP_VIEW)
    public ResponseEntity<Page<MenuMasterResponse>> findByHotelBranch(@PathVariable("hotelBranchId") UUID hotelBranchId, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(menuMasterService.findByHotelBranchId(hotelBranchId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize(RestaurantPermissions.Has.SETUP_VIEW)
    public ResponseEntity<MenuMasterResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(menuMasterService.findOne(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(RestaurantPermissions.Has.SETUP_MANAGE)
    public ResponseEntity<MenuMasterResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(menuMasterService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(RestaurantPermissions.Has.SETUP_MANAGE)
    public ResponseEntity<MenuMasterResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(menuMasterService.deactivate(id));
    }

    @PatchMapping("/{id}/update-price")
    @PreAuthorize(RestaurantPermissions.Has.MENU_PRICE_MANAGE)
    public ResponseEntity<MenuMasterResponse> updatePrice(@PathVariable("id") UUID id, @Valid @RequestBody UpdatePriceRequest updatePriceRequest){
        return ResponseEntity.ok(menuMasterService.updateUnitPrice(id, updatePriceRequest.unitPrice()));
    }

}
