package rw.afriteck.pms.platform.service;

import rw.afriteck.pms.platform.dtos.OnboardHotelRequest;
import rw.afriteck.pms.platform.dtos.OnboardHotelResponse;

public interface HotelOnboardingService {

    OnboardHotelResponse onboard(OnboardHotelRequest request);
}
