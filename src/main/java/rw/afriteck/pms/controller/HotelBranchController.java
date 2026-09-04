package rw.afriteck.pms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IRestaurantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel-branch")
@RequiredArgsConstructor
public class HotelBranchController {
    private final IHotelBranchService hotelBranchService;
    private final IRestaurantService restaurantService;

    @PostMapping("/register")
    public ResponseEntity<HotelBranchResponse> registerHotelBranch(@RequestBody CreateHotelBranchRequest hotelBranchRequest){
        HotelBranchResponse hotelBranch = this.hotelBranchService.create(hotelBranchRequest);
        return ResponseEntity.ok(hotelBranch);
    }

    @GetMapping("/all")
    public ResponseEntity<List<HotelBranchResponse>> findAll(){
        return ResponseEntity.ok(this.hotelBranchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelBranchResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelBranchService.findOne(id));
    }

    @GetMapping("/{id}/restaurant")
    public ResponseEntity<List<RestaurantResponse>> findRestaurant(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.findByHotelBranch(id));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<HotelBranchResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelBranchService.activate(id));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<HotelBranchResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelBranchService.deactivate(id));
    }
}
