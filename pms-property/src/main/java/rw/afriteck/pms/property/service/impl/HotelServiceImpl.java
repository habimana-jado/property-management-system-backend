package rw.afriteck.pms.property.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
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
    public HotelResponse create(CreateHotelRequest hotelRequest) {
        Hotel hotel = hotelMapper.toEntity(hotelRequest);
        hotel.setStatus(ERecordStatus.ACTIVE);
        Hotel savedEntity = hotelRepo.save(hotel);
        return hotelMapper.toResponse(savedEntity);
    }

    // HotelServiceImpl
    @Override
    @Transactional(readOnly = true)
    public HotelResponse findCurrent() {
        return findOne(TenantContext.require());
    }

    @Override
    @Transactional
    public HotelResponse updateCurrent(CreateHotelRequest request) {
        return update(TenantContext.require(), request);
    }

    @Override
    @Transactional
    public HotelResponse update(UUID hotelId, CreateHotelRequest hotelRequest) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        hotelMapper.updateEntityFromRequest(hotelRequest, hotel);
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> findAll(Pageable pageable) {
        return hotelRepo.findAll(pageable)
                .map(hotelMapper::toResponse);
    }

    @Override
    public HotelResponse findOne(UUID hotelId) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        return hotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional
    public HotelResponse activate(UUID hotelId) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        hotel.setStatus(ERecordStatus.ACTIVE);
        Hotel activatedHotel = hotelRepo.save(hotel);
        return hotelMapper.toResponse(activatedHotel);
    }

    @Override
    @Transactional
    public HotelResponse deactivate(UUID hotelId) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
        hotel.setStatus(ERecordStatus.INACTIVE);
        return hotelMapper.toResponse(hotelRepo.save(hotel));
    }
}
