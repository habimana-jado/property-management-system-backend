package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.auth.dtos.StaffRegistrationRequest;
import rw.afriteck.pms.auth.dtos.StaffResponse;
import rw.afriteck.pms.auth.dtos.UpdateStaffProfileRequest;
import rw.afriteck.pms.auth.service.StaffService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public StaffResponse register(@Valid @RequestBody StaffRegistrationRequest request) {
        return staffService.registerStaff(request);
    }

    @PutMapping("/{staffId}")
    public StaffResponse updateProfile(@PathVariable UUID staffId,
                                       @Valid @RequestBody UpdateStaffProfileRequest request) {
        return staffService.updateProfile(staffId, request);
    }

    @GetMapping("/{staffId}")
    public StaffResponse getStaff(@PathVariable UUID staffId) {
        return staffService.getStaff(staffId);
    }

    @GetMapping("hotel-branches/{id}")
    public List<StaffResponse> findByHotelBranch(@PathVariable("id") UUID hotelBranchId, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return staffService.findByHotelBranch(hotelBranchId, pageable);
    }

}
