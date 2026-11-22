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
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;
import rw.afriteck.pms.auth.dtos.PlatformPrincipal;
import rw.afriteck.pms.auth.service.PlatformAuthService;
import rw.afriteck.pms.common.exception.ForbiddenException;

@RestController
@RequestMapping("/api/v1/pms/platform/auth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final PlatformAuthService platformAuthService;

    @PostMapping("/login")
    public PlatformLoginResponse login(@Valid @RequestBody LoginRequest request) {
        return platformAuthService.login(request);
    }

    @PostMapping("/change-password")
    public PlatformLoginResponse changePassword(@AuthenticationPrincipal PlatformPrincipal principal,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        if (principal == null) {   // e.g. a hotel staff token
            throw new ForbiddenException("OPERATOR_ONLY", "Only platform operators can use this endpoint");
        }
        return platformAuthService.changePassword(principal.operatorId(), request);
    }

}
