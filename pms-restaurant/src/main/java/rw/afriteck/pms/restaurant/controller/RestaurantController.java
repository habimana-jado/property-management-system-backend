package rw.afriteck.pms.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.restaurant.dtos.*;
import rw.afriteck.pms.restaurant.service.MenuCategoryService;
import rw.afriteck.pms.restaurant.service.MenuMasterService;
import rw.afriteck.pms.restaurant.service.RestaurantService;
import rw.afriteck.pms.restaurant.service.TableMasterService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final TableMasterService tableMasterService;
    private final MenuCategoryService menuCategoryService;
    private final MenuMasterService menuMasterService;

    @PostMapping
    public ResponseEntity<RestaurantResponse> registerRestaurant(@Valid @RequestBody CreateRestaurantRequest restaurantRequest){
        RestaurantResponse restaurantResponse = this.restaurantService.create(restaurantRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(restaurantResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(restaurantResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(@PathVariable("id") UUID id, @Valid @RequestBody CreateRestaurantRequest restaurantRequest){
        RestaurantResponse restaurantResponse = this.restaurantService.update(id, restaurantRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(restaurantResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(restaurantResponse);
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> findByHotelBranch(@RequestParam UUID hotelBranchId, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return hotelBranchId != null
                ? ResponseEntity.ok(restaurantService.findByHotelBranchAndActive(hotelBranchId, pageable))
                : ResponseEntity.ok(this.restaurantService.findAll(pageable));
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

    @GetMapping("/{id}/menu-items")
    public ResponseEntity<Page<MenuMasterResponse>> findMenuItems(@PathVariable UUID id, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(menuMasterService.findByRestaurantAndActive(id, pageable));
    }

    @GetMapping("/{restaurantId}/menu-items/search")
    public ResponseEntity<List<MenuItemsByCategoryResponse>> search(@PathVariable UUID restaurantId, @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(restaurantService.searchMenuItemsGrouped(restaurantId, keyword));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RestaurantResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(restaurantService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RestaurantResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(restaurantService.deactivate(id));
    }

}
