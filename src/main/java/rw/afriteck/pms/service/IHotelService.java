package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.HotelRequest;
import rw.afriteck.pms.model.Hotel;

import java.util.List;
import java.util.UUID;

public interface IHotelService {
    public Hotel registerHotel(HotelRequest hotelRequest);
    public List<Hotel> findAll();
    public Hotel findOne(UUID hotelId);
}
