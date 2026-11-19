package rw.afriteck.pms.auth.service;

import rw.afriteck.pms.auth.dtos.ProvisionAdminRequest;
import rw.afriteck.pms.auth.dtos.ProvisionedAdmin;

import java.util.UUID;

public interface HotelAccessProvisioningService {
    ProvisionedAdmin provisionHotelAdmin(UUID hotelId, ProvisionAdminRequest request);
}
