package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.LoginRequest;
import rw.afriteck.pms.dtos.LoginResponse;
import rw.afriteck.pms.dtos.RefreshTokenRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);
}
