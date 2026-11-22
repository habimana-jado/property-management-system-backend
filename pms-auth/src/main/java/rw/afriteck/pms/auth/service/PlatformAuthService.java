package rw.afriteck.pms.auth.service;

import rw.afriteck.pms.auth.dtos.ChangePasswordRequest;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;

import java.util.UUID;

public interface PlatformAuthService {
    PlatformLoginResponse login(LoginRequest request);

    PlatformLoginResponse changePassword(UUID operatorId, ChangePasswordRequest request);
}
