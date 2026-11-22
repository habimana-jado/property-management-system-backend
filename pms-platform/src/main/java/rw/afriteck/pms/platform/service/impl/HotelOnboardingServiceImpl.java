package rw.afriteck.pms.platform.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.ProvisionedAdmin;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.auth.service.HotelAccessProvisioningService;
import rw.afriteck.pms.platform.dtos.OnboardHotelRequest;
import rw.afriteck.pms.platform.dtos.OnboardHotelResponse;
import rw.afriteck.pms.platform.service.HotelOnboardingService;
import rw.afriteck.pms.property.dtos.HotelResponse;
import rw.afriteck.pms.property.service.HotelService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelOnboardingServiceImpl implements HotelOnboardingService {

    private final HotelService hotelService;
    private final HotelAccessProvisioningService provisioningService;

    @Transactional
    @Override
    public OnboardHotelResponse onboard(OnboardHotelRequest request) {
        HotelResponse hotel = hotelService.create(request.hotel());
        ProvisionedAdmin admin = provisioningService.provisionHotelAdmin(hotel.id(), hotel.hotelCode(), request.admin());
        return new OnboardHotelResponse(hotel, admin.staffId(), admin.userId(), admin.username());
    }

    @Transactional
    @Override
    public TemporaryPasswordResponse resetStaffPassword(UUID hotelId, UUID staffId) {
        return provisioningService.resetStaffPassword(hotelId, staffId);
    }

}
