package rw.afriteck.pms.property.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.tenant.TenantContext;
import rw.afriteck.pms.property.dtos.CreateHotelRequest;
import rw.afriteck.pms.property.dtos.HotelResponse;
import rw.afriteck.pms.property.mapper.HotelMapper;
import rw.afriteck.pms.property.model.Hotel;
import rw.afriteck.pms.property.repository.HotelRepo;
import rw.afriteck.pms.property.service.HotelService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepo hotelRepo;
    private final HotelMapper hotelMapper;

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_HOTELS')")
    public HotelResponse create(CreateHotelRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        hotel.setStatus(ERecordStatus.ACTIVE);

        if (hotelRepo.existsByHotelCode(request.hotelCode())) {
            throw new DuplicateResourceException("Hotel code already in use: " + request.hotelCode());
        }

        return hotelMapper.toResponse(hotelRepo.save(hotel));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PLATFORM_VIEW_HOTELS')")
    public Page<HotelResponse> findAll(Pageable pageable) {
        return hotelRepo.findAll(pageable).map(hotelMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PLATFORM_VIEW_HOTELS')")
    public HotelResponse findOne(UUID hotelId) {
        return hotelMapper.toResponse(getOrThrow(hotelId));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_HOTELS')")
    public HotelResponse update(UUID hotelId, CreateHotelRequest request) {
        Hotel hotel = getOrThrow(hotelId);
        requireSameHotelCode(hotel, request);
        hotelMapper.updateEntityFromRequest(request, hotel);
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_HOTELS')")
    public HotelResponse activate(UUID hotelId) {
        Hotel hotel = getOrThrow(hotelId);
        hotel.setStatus(ERecordStatus.ACTIVE);
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_HOTELS')")
    public HotelResponse deactivate(UUID hotelId) {
        Hotel hotel = getOrThrow(hotelId);
        hotel.setStatus(ERecordStatus.INACTIVE);
        return hotelMapper.toResponse(hotel);
    }

    // tenant-bound: any hotel staff member can read their own hotel
    @Override
    @Transactional(readOnly = true)
    public HotelResponse findCurrent() {
        return hotelMapper.toResponse(getOrThrow(TenantContext.require()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('HOTEL_PROFILE_MANAGE')")
    public HotelResponse updateCurrent(CreateHotelRequest request) {
        Hotel hotel = getOrThrow(TenantContext.require());
        requireSameHotelCode(hotel, request);
        hotelMapper.updateEntityFromRequest(request, hotel);
        return hotelMapper.toResponse(hotel);
    }

    private Hotel getOrThrow(UUID hotelId) {
        return hotelRepo.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
    }

    private void requireSameHotelCode(Hotel hotel, CreateHotelRequest request) {
        if (!hotel.getHotelCode().equals(request.hotelCode())) {
            throw new BusinessRuleViolationException("HOTEL_CODE_IMMUTABLE",
                    "The hotel code cannot be changed after creation");
        }
    }
}
