package rw.afriteck.pms.service.impl;

import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.HotelRequest;
import rw.afriteck.pms.enums.EStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.repository.HotelRepo;
import rw.afriteck.pms.service.IHotelService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HotelServiceImpl implements IHotelService {
    private final HotelRepo hotelRepo;
    private final ObjectMapper objectMapper;

    public HotelServiceImpl(HotelRepo hotelRepo, ObjectMapper objectMapper){
        this.hotelRepo = hotelRepo;
        this.objectMapper = objectMapper;
    }
    @Override
    public Hotel registerHotel(HotelRequest hotelRequest) {
        Hotel hotel = objectMapper.convertValue(hotelRequest, Hotel.class);
        hotel.setStatus(EStatus.ACTIVE);
        return this.hotelRepo.save(hotel);
    }

    @Override
    public List<Hotel> findAll() {
        return this.hotelRepo.findAll();
    }

    @Override
    public Hotel findOne(UUID hotelId) {
        return this.hotelRepo.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", hotelId));
    }

    @Override
    public Hotel activate(UUID hotelId) {
        Hotel hotel = this.hotelRepo.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", hotelId));
        hotel.setStatus(EStatus.ACTIVE);
        return this.hotelRepo.save(hotel);
    }

    @Override
    public Hotel deactivate(UUID hotelId) {
        Hotel hotel = this.hotelRepo.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel", hotelId));
        hotel.setStatus(EStatus.INACTIVE);
        return this.hotelRepo.save(hotel);
    }
}
