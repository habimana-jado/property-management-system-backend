package rw.afriteck.pms.property.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.tenant.HotelDirectory;
import rw.afriteck.pms.property.repository.HotelBranchRepo;
import rw.afriteck.pms.property.repository.HotelRepo;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HotelDirectoryImpl implements HotelDirectory {

    private final HotelRepo hotelRepo;
    private final HotelBranchRepo hotelBranchRepo;

    @Override
    @Transactional(readOnly = true)
    public boolean isHotelActive(UUID hotelId) {
        return hotelRepo.existsByIdAndStatus(hotelId, ERecordStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean branchBelongsToHotel(UUID branchId, UUID hotelId) {
        return hotelBranchRepo.existsByIdAndHotelId(branchId, hotelId);
    }
}