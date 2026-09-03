package rw.afriteck.pms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.HotelBranchRequest;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.service.IHotelBranchService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel-branch")
public class HotelBranchController {
    private final IHotelBranchService hotelBranchService;

    public HotelBranchController(IHotelBranchService hotelBranchService){
        this.hotelBranchService = hotelBranchService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerHotelBranch(@RequestBody HotelBranchRequest hotelBranchRequest){
        HotelBranch hotelBranch = this.hotelBranchService.registerHotelBranch(hotelBranchRequest);
        return ResponseEntity.ok(hotelBranch);
    }

    @GetMapping("/all")
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(this.hotelBranchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOne(@PathVariable("id") String id){
        return ResponseEntity.ok(this.hotelBranchService.findOne(UUID.fromString(id)));
    }

    @GetMapping("/hotel/{id}")
    public ResponseEntity<?> findByHotel(@PathVariable("id") String id){
        return ResponseEntity.ok(this.hotelBranchService.findByHotel(UUID.fromString(id)));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activate(@PathVariable("id") String id){
        return ResponseEntity.ok(this.hotelBranchService.activate(UUID.fromString(id)));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable("id") String id){
        return ResponseEntity.ok(this.hotelBranchService.deactivate(UUID.fromString(id)));
    }
}
