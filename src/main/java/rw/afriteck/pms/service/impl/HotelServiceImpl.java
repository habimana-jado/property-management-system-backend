package rw.afriteck.pms.service.impl;

import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.HotelRequest;
import rw.afriteck.pms.enums.EStatus;
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
        Optional<Hotel> hotel = this.hotelRepo.findById(hotelId);
        return hotel.orElseThrow(null);
    }

    @Override
    public Hotel activate(UUID hotelId) {
        Optional<Hotel> hotel = this.hotelRepo.findById(hotelId);
        if(hotel.isPresent()){
            Hotel hotel1 = hotel.get();
            hotel1.setStatus(EStatus.ACTIVE);
            return this.hotelRepo.save(hotel1);
        }else{
            return null;
        }
    }

    @Override
    public Hotel deactivate(UUID hotelId) {
        Optional<Hotel> hotel = this.hotelRepo.findById(hotelId);
        if(hotel.isPresent()){
            Hotel hotel1 = hotel.get();
            hotel1.setStatus(EStatus.INACTIVE);
            return this.hotelRepo.save(hotel1);
        }else{
            return null;
        }
    }
}
