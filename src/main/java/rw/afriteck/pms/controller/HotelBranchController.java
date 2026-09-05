package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IRestaurantService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel-branches")
@RequiredArgsConstructor
public class HotelBranchController {
    private final IHotelBranchService hotelBranchService;
    private final IRestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<HotelBranchResponse> registerHotelBranch(@Valid @RequestBody CreateHotelBranchRequest hotelBranchRequest){
        HotelBranchResponse hotelBranch = this.hotelBranchService.create(hotelBranchRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(hotelBranch.id())
                .toUri();
        return ResponseEntity.created(location).body(hotelBranch);
    }

    @GetMapping
    public ResponseEntity<Page<HotelBranchResponse>> findAll(@PageableDefault(size = 20, sort = "id")Pageable pageable){
        return ResponseEntity.ok(this.hotelBranchService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelBranchResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelBranchService.findOne(id));
    }

    @GetMapping("/{id}/restaurants")
    public ResponseEntity<Page<RestaurantResponse>> findRestaurant(@PathVariable("id") UUID id, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(this.restaurantService.findByHotelBranchAndActive(id, pageable));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<HotelBranchResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelBranchService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<HotelBranchResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelBranchService.deactivate(id));
    }
}
