package rw.afriteck.pms.auth.service;

import rw.afriteck.pms.auth.dtos.ChangePasswordRequest;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.LoginResponse;
import rw.afriteck.pms.auth.dtos.RefreshTokenRequest;

import java.util.UUID;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    LoginResponse changePassword(UUID userId, ChangePasswordRequest request);
}
