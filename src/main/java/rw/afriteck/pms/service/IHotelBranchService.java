package rw.afriteck.pms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.model.HotelBranch;

import java.util.List;
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
