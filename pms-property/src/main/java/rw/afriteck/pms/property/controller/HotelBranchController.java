package rw.afriteck.pms.property.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.property.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.property.dtos.HotelBranchResponse;
import rw.afriteck.pms.property.service.IHotelBranchService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel-branches")
@RequiredArgsConstructor
public class HotelBranchController {
    private final IHotelBranchService hotelBranchService;

    @PostMapping
    public ResponseEntity<HotelBranchResponse> registerHotelBranch(@Valid @RequestBody CreateHotelBranchRequest hotelBranchRequest){
        HotelBranchResponse hotelBranch = hotelBranchService.create(hotelBranchRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(hotelBranch.id())
                .toUri();
        return ResponseEntity.created(location).body(hotelBranch);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelBranchResponse> updateHotelBranch(@PathVariable("id") UUID id, @Valid @RequestBody CreateHotelBranchRequest hotelBranchRequest){
        HotelBranchResponse hotelBranch = hotelBranchService.update(id, hotelBranchRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(hotelBranch.id())
                .toUri();
        return ResponseEntity.created(location).body(hotelBranch);
    }

    @GetMapping
    public ResponseEntity<Page<HotelBranchResponse>> findAll(@PageableDefault(size = 20, sort = "id")Pageable pageable){
        return ResponseEntity.ok(hotelBranchService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelBranchResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelBranchService.findOne(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<HotelBranchResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelBranchService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<HotelBranchResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelBranchService.deactivate(id));
    }
}
