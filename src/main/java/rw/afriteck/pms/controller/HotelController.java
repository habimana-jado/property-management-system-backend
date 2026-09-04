package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.CreateHotelRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.dtos.HotelResponse;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IHotelService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel")
@RequiredArgsConstructor
public class HotelController {
    private final IHotelService hotelService;
    private final IHotelBranchService hotelBranchService;

    @PostMapping("/register")
    public ResponseEntity<HotelResponse> registerHotel(@Valid @RequestBody CreateHotelRequest hotelRequest){
        HotelResponse hotel = this.hotelService.create(hotelRequest);
        return ResponseEntity.ok(hotel);
    }

    @GetMapping("/all")
    public ResponseEntity<List<HotelResponse>> findAll(){
        return ResponseEntity.ok(this.hotelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> findOne(@PathVariable("id")UUID id){
        return ResponseEntity.ok(this.hotelService.findOne(id));
    }

    @GetMapping("/{id}/hotel-branch")
    public ResponseEntity<List<HotelBranchResponse>> findHotelBranch(@PathVariable("id")UUID id){
        return ResponseEntity.ok(this.hotelBranchService.findByHotel(id));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<HotelResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelService.activate(id));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<HotelResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelService.deactivate(id));
    }
}
