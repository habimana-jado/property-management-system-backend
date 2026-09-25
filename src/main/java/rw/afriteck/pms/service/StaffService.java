package rw.afriteck.pms.service;

import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.dtos.StaffRegistrationRequest;
import rw.afriteck.pms.dtos.StaffResponse;
import rw.afriteck.pms.dtos.UpdateStaffProfileRequest;

import java.util.List;
import java.util.UUID;

public interface StaffService {

    StaffResponse registerStaff(StaffRegistrationRequest request);

    StaffResponse updateProfile(UUID staffId, UpdateStaffProfileRequest request);

    StaffResponse getStaff(UUID staffId);

    List<StaffResponse> findByHotelBranch(UUID hotelBranchId, Pageable pageable);
}
