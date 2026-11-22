package rw.afriteck.pms.platform.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.platform.dtos.OnboardHotelRequest;
import rw.afriteck.pms.platform.dtos.OnboardHotelResponse;

import java.util.UUID;

public interface HotelOnboardingService {

    OnboardHotelResponse onboard(OnboardHotelRequest request);

    TemporaryPasswordResponse resetStaffPassword(UUID hotelId, UUID staffId);
}
