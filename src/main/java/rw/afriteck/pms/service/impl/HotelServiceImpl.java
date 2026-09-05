package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.CreateHotelRequest;
import rw.afriteck.pms.dtos.HotelResponse;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.HotelMapper;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.repository.HotelRepo;
import rw.afriteck.pms.service.IHotelService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements IHotelService {

    private final HotelRepo hotelRepo;
    private final HotelMapper hotelMapper;

    @Override
    public HotelResponse create(CreateHotelRequest hotelRequest) {
        Hotel hotel = hotelMapper.toEntity(hotelRequest);
        hotel.setStatus(ERecordStatus.ACTIVE);
        Hotel savedEntity = hotelRepo.save(hotel);
        return hotelMapper.toResponse(savedEntity);
    }

    @Override
    public HotelResponse update(UUID hotelId, CreateHotelRequest hotelRequest) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel", hotelId));

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
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", hotelId));
        return hotelMapper.toResponse(hotel);
    }

    @Override
    public HotelResponse activate(UUID hotelId) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", hotelId));
        hotel.setStatus(ERecordStatus.ACTIVE);
        Hotel activatedHotel = hotelRepo.save(hotel);
        return hotelMapper.toResponse(activatedHotel);
    }

    @Override
    public HotelResponse deactivate(UUID hotelId) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", hotelId));
        hotel.setStatus(ERecordStatus.INACTIVE);
        return hotelMapper.toResponse(hotelRepo.save(hotel));
    }
}
