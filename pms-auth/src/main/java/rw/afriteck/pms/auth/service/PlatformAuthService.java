package rw.afriteck.pms.auth.service;

import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;

public interface PlatformAuthService {
    PlatformLoginResponse login(LoginRequest request);
}
