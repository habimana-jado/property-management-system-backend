package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.dtos.MenuMasterResponse;
import rw.afriteck.pms.dtos.UpdatePriceRequest;
import rw.afriteck.pms.service.IMenuMasterService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/menu-items")
@RequiredArgsConstructor
public class MenuMasterController {
    private final IMenuMasterService menuMasterService;

    @PostMapping
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
    public ResponseEntity<Page<MenuMasterResponse>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(menuMasterService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuMasterResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(menuMasterService.findOne(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<MenuMasterResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(menuMasterService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<MenuMasterResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(menuMasterService.deactivate(id));
    }

    @PatchMapping("/{id}/update-price")
    public ResponseEntity<MenuMasterResponse> updatePrice(@PathVariable("id") UUID id, @Valid @RequestBody UpdatePriceRequest updatePriceRequest){
        return ResponseEntity.ok(menuMasterService.updateUnitPrice(id, updatePriceRequest.unitPrice()));
    }

}
