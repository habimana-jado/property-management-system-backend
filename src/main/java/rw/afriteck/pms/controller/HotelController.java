package rw.afriteck.pms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.HotelRequest;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.service.IHotelService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel")
public class HotelController {
    private final IHotelService hotelService;

    public HotelController(IHotelService hotelService){
        this.hotelService = hotelService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHotel(@RequestBody HotelRequest hotelRequest){
        Hotel hotel = this.hotelService.registerHotel(hotelRequest);
        return ResponseEntity.ok(hotel);
    }

    @GetMapping("/all")
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(this.hotelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOne(@PathVariable("id")UUID id){
        return ResponseEntity.ok(this.hotelService.findOne(id));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelService.activate(id));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.hotelService.deactivate(id));
    }
}
