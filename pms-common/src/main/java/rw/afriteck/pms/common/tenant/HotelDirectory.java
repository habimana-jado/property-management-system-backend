package rw.afriteck.pms.common.tenant;

import java.util.UUID;

public interface HotelDirectory {
    boolean isHotelActive(UUID hotelId);

    boolean branchBelongsToHotel(UUID branchId, UUID hotelId);
}
