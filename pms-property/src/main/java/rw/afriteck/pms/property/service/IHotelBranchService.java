package rw.afriteck.pms.property.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.property.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.property.dtos.HotelBranchResponse;

import java.util.UUID;

public interface IHotelBranchService {

    HotelBranchResponse create(CreateHotelBranchRequest hotelBranchRequest);

    HotelBranchResponse update(UUID id, CreateHotelBranchRequest hotelBranchRequest);

    Page<HotelBranchResponse> findAll(Pageable pageable);

    HotelBranchResponse findOne(UUID hotelBranchId);

    HotelBranchResponse activate(UUID hotelBranchId);

    HotelBranchResponse deactivate(UUID hotelBranchId);

    Page<HotelBranchResponse> findByHotelAndActive(UUID hotelId, Pageable pageable);
}
