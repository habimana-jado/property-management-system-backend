package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.dtos.CreateHotelRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.dtos.HotelResponse;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IHotelService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotels")
@RequiredArgsConstructor
public class HotelController {
    private final IHotelService hotelService;
    private final IHotelBranchService hotelBranchService;

    @PostMapping
    public ResponseEntity<HotelResponse> registerHotel(@Valid @RequestBody CreateHotelRequest hotelRequest){
        HotelResponse hotel = hotelService.create(hotelRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(hotel.id())
                .toUri();
        return ResponseEntity.created(location).body(hotel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(@PathVariable("id") UUID id, @Valid @RequestBody CreateHotelRequest hotelRequest){
        HotelResponse hotel = hotelService.update(id, hotelRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(hotel.id())
                .toUri();
        return ResponseEntity.created(location).body(hotel);
    }

    @GetMapping
    public ResponseEntity<Page<HotelResponse>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(hotelService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> findOne(@PathVariable("id")UUID id){
        return ResponseEntity.ok(hotelService.findOne(id));
    }

    @GetMapping("/{id}/hotel-branches")
    public ResponseEntity<Page<HotelBranchResponse>> findHotelBranch(@PathVariable("id")UUID id, @PageableDefault(size = 20, sort = "id") Pageable pageable){
        return ResponseEntity.ok(hotelBranchService.findByHotelAndActive(id, pageable));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<HotelResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<HotelResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelService.deactivate(id));
    }
}
