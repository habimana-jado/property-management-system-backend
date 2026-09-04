package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.CreateHotelRequest;
import rw.afriteck.pms.dtos.HotelResponse;
import rw.afriteck.pms.model.Hotel;

import java.util.List;
import java.util.UUID;

public interface IHotelService {
    public HotelResponse create(CreateHotelRequest hotelRequest);
    public List<HotelResponse> findAll();
    public HotelResponse findOne(UUID hotelId);
    public HotelResponse activate(UUID hotelId);
    public HotelResponse deactivate(UUID hotelId);
}
