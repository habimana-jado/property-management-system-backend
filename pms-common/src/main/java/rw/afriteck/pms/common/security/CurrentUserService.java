package rw.afriteck.pms.common.security;

import java.util.UUID;

public interface CurrentUserService {
    UUID requireCurrentStaffId();

    UUID requireCurrentUserId();

    UUID requireCurrentHotelBranchId();
}
