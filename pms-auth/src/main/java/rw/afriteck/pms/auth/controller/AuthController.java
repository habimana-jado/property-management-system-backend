package rw.afriteck.pms.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.afriteck.pms.auth.dtos.ChangePasswordRequest;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.LoginResponse;
import rw.afriteck.pms.auth.dtos.RefreshTokenRequest;
import rw.afriteck.pms.auth.security.StaffPrincipal;
import rw.afriteck.pms.auth.service.AuthService;
import rw.afriteck.pms.common.exception.ForbiddenException;

@RestController
@RequestMapping("/api/v1/pms/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/change-password")
    public LoginResponse changePassword(@AuthenticationPrincipal StaffPrincipal principal,
                                        @Valid @RequestBody ChangePasswordRequest request) {
        if (principal == null) {   // e.g. a platform operator's token
            throw new ForbiddenException("STAFF_ONLY", "Only hotel staff can use this endpoint");
        }
        return authService.changePassword(principal.userId(), request);
    }
}
