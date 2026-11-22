package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.auth.dtos.StaffRegistrationRequest;
import rw.afriteck.pms.auth.dtos.StaffResponse;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.auth.dtos.UpdateStaffProfileRequest;
import rw.afriteck.pms.auth.security.AuthPermissions;
import rw.afriteck.pms.auth.service.StaffService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    @PreAuthorize(AuthPermissions.Has.STAFF_MANAGE)
    public StaffResponse register(@Valid @RequestBody StaffRegistrationRequest request) {
        return staffService.registerStaff(request);
    }

    @PutMapping("/{staffId}")
    @PreAuthorize(AuthPermissions.Has.STAFF_MANAGE)
    public StaffResponse updateProfile(@PathVariable UUID staffId,
                                       @Valid @RequestBody UpdateStaffProfileRequest request) {
        return staffService.updateProfile(staffId, request);
    }

    @PostMapping("/{staffId}/reset-password")
    @PreAuthorize(AuthPermissions.Has.STAFF_PASSWORD_RESET)
    public ResponseEntity<TemporaryPasswordResponse> resetPassword(@PathVariable UUID staffId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())          // the response contains a secret
                .body(staffService.resetPassword(staffId));
    }

    @GetMapping("/{staffId}")
    @PreAuthorize(AuthPermissions.Has.STAFF_VIEW)
    public StaffResponse getStaff(@PathVariable UUID staffId) {
        return staffService.getStaff(staffId);
    }

    @GetMapping("hotel-branches/{id}")
    @PreAuthorize(AuthPermissions.Has.STAFF_VIEW)
    public List<StaffResponse> findByHotelBranch(@PathVariable("id") UUID hotelBranchId, @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return staffService.findByHotelBranch(hotelBranchId, pageable);
    }

}
