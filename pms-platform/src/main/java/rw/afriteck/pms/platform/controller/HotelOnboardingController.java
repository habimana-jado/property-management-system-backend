package rw.afriteck.pms.platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.afriteck.pms.platform.dtos.OnboardHotelRequest;
import rw.afriteck.pms.platform.dtos.OnboardHotelResponse;
import rw.afriteck.pms.platform.service.HotelOnboardingService;

@RestController
@RequestMapping("/api/v1/pms/platform/hotels")
@RequiredArgsConstructor
public class HotelOnboardingController {

    private final HotelOnboardingService onboardingService;

    @PostMapping("/onboard")
    public ResponseEntity<OnboardHotelResponse> onboard(@Valid @RequestBody OnboardHotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(onboardingService.onboard(request));
    }
}
