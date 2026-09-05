package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.CreateRestaurantRequest;
import rw.afriteck.pms.dtos.MenuCategoryResponse;
import rw.afriteck.pms.dtos.RestaurantResponse;
import rw.afriteck.pms.dtos.TableMasterResponse;
import rw.afriteck.pms.service.IMenuCategoryService;
import rw.afriteck.pms.service.IRestaurantService;
import rw.afriteck.pms.service.ITableMasterService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final IRestaurantService restaurantService;
    private final ITableMasterService tableMasterService;
    private final IMenuCategoryService menuCategoryService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> registerRestaurant(@Valid @RequestBody CreateRestaurantRequest restaurantRequest){
        return ResponseEntity.ok(this.restaurantService.create(restaurantRequest));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(this.restaurantService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.findOne(id));
    }

    @GetMapping("/{id}/tables")
    public ResponseEntity<Page<TableMasterResponse>> findTables(@PathVariable("id") UUID id, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(this.tableMasterService.findByRestaurantAndActive(id, pageable));
    }

    @GetMapping("/{id}/menu-categories")
    public ResponseEntity<Page<MenuCategoryResponse>> findMenuCategories(@PathVariable UUID id, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(menuCategoryService.findByRestaurant(id, pageable));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RestaurantResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RestaurantResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(this.restaurantService.deactivate(id));
    }

}
