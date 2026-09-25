package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.dtos.StaffRegistrationRequest;
import rw.afriteck.pms.dtos.StaffResponse;
import rw.afriteck.pms.dtos.UpdateStaffProfileRequest;
import rw.afriteck.pms.service.StaffService;

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

}
