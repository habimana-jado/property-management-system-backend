package rw.afriteck.pms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.service.IRestaurantService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    private final IRestaurantService restaurantService;

    @PostMapping("/register")
    public ResponseEntity<RestaurantResponse> registerRestaurant(@RequestBody CreateRestaurantRequest restaurantRequest){
        return ResponseEntity.ok(this.restaurantService.create(restaurantRequest));
    }

    @GetMapping("/all")
    public ResponseEntity<List<RestaurantResponse>> findAll(){
        return ResponseEntity.ok(this.restaurantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.findOne(id));
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<RestaurantResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.activate(id));
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<RestaurantResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.deactivate(id));
    }

}
