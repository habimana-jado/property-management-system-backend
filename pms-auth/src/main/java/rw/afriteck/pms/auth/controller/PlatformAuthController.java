package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;
import rw.afriteck.pms.auth.service.PlatformAuthService;

@RestController
@RequestMapping("/api/v1/pms/platform/auth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final PlatformAuthService platformAuthService;

    @PostMapping("/login")
    public PlatformLoginResponse login(@Valid @RequestBody LoginRequest request) {
        return platformAuthService.login(request);
    }
}
