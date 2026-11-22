package rw.afriteck.pms.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.common.security.PlatformPermissions;
import rw.afriteck.pms.platform.dtos.OnboardHotelRequest;
import rw.afriteck.pms.platform.dtos.OnboardHotelResponse;
import rw.afriteck.pms.platform.service.HotelOnboardingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/platform/hotels")
@RequiredArgsConstructor
public class HotelOnboardingController {

    private final HotelOnboardingService onboardingService;

    @PostMapping("/onboard")
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public ResponseEntity<OnboardHotelResponse> onboard(@Valid @RequestBody OnboardHotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.onboard(request));
    }

    @PostMapping("/{hotelId}/staff/{staffId}/reset-password")
    @PreAuthorize(PlatformPermissions.Has.MANAGE_HOTELS)
    public ResponseEntity<TemporaryPasswordResponse> resetStaffPassword(@PathVariable UUID hotelId,
                                                                        @PathVariable UUID staffId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(onboardingService.resetStaffPassword(hotelId, staffId));
    }
}
