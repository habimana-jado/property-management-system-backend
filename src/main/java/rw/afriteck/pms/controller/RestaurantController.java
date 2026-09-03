package rw.afriteck.pms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.RestaurantRequest;
import rw.afriteck.pms.service.IRestaurantService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/restaurant")
public class RestaurantController {
    private final IRestaurantService restaurantService;

    public RestaurantController(IRestaurantService restaurantService){
        this.restaurantService = restaurantService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerRestaurant(@RequestBody RestaurantRequest restaurantRequest){
        return ResponseEntity.ok(this.restaurantService.registerRestaurant(restaurantRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(this.restaurantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOne(@PathVariable("id") String id){
        return ResponseEntity.ok(this.restaurantService.findOne(UUID.fromString(id)));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<?> activate(@PathVariable("id") String id){
        return ResponseEntity.ok(this.restaurantService.activate(UUID.fromString(id)));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable("id") String id){
        return ResponseEntity.ok(this.restaurantService.deactivate(UUID.fromString(id)));
    }

    @GetMapping("/hotel-branch/{id}")
    public ResponseEntity<?> findByHotelBranch(@PathVariable("id") String id){
        return ResponseEntity.ok(this.restaurantService.findByHotelBranch(UUID.fromString(id)));
    }

}
