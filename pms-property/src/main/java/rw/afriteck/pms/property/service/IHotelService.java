package rw.afriteck.pms.property.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.property.dtos.CreateHotelRequest;
import rw.afriteck.pms.property.dtos.HotelResponse;

import java.util.UUID;

public interface IHotelService {

    HotelResponse create(CreateHotelRequest hotelRequest);

    HotelResponse update(UUID hotelId, CreateHotelRequest hotelRequest);

    Page<HotelResponse> findAll(Pageable pageable);

    HotelResponse findOne(UUID hotelId);

    HotelResponse activate(UUID hotelId);

    HotelResponse deactivate(UUID hotelId);
}
