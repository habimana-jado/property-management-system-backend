package rw.afriteck.pms.property.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.property.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.property.dtos.HotelBranchResponse;
import rw.afriteck.pms.property.security.HotelPermissions;
import rw.afriteck.pms.property.service.HotelBranchService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/hotel-branches")
@RequiredArgsConstructor
public class HotelBranchController {
    private final HotelBranchService hotelBranchService;

    @PostMapping
    @PreAuthorize(HotelPermissions.Has.BRANCH_MANAGE)
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
    @PreAuthorize(HotelPermissions.Has.BRANCH_MANAGE)
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
    @PreAuthorize(HotelPermissions.Has.BRANCH_VIEW)
    public ResponseEntity<Page<HotelBranchResponse>> findAll(@PageableDefault(size = 20, sort = "id")Pageable pageable){
        return ResponseEntity.ok(hotelBranchService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize(HotelPermissions.Has.BRANCH_VIEW)
    public ResponseEntity<HotelBranchResponse> findOne(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelBranchService.findOne(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(HotelPermissions.Has.BRANCH_MANAGE)
    public ResponseEntity<HotelBranchResponse> activate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelBranchService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(HotelPermissions.Has.BRANCH_MANAGE)
    public ResponseEntity<HotelBranchResponse> deactivate(@PathVariable("id") UUID id){
        return ResponseEntity.ok(hotelBranchService.deactivate(id));
    }
}
