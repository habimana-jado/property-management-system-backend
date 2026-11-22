package rw.afriteck.pms.auth.service;

import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.ProvisionAdminRequest;
import rw.afriteck.pms.auth.dtos.ProvisionedAdmin;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;

import java.util.UUID;

public interface HotelAccessProvisioningService {
    ProvisionedAdmin provisionHotelAdmin(UUID hotelId, String hotelCode, ProvisionAdminRequest request);

    TemporaryPasswordResponse resetStaffPassword(UUID hotelId, UUID staffId);
}
