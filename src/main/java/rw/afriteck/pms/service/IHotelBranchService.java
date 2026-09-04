package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.model.HotelBranch;

import java.util.List;
import java.util.UUID;

public interface IHotelBranchService {
    public HotelBranchResponse create(CreateHotelBranchRequest hotelBranchRequest);
    public List<HotelBranchResponse> findAll();
    public HotelBranchResponse findOne(UUID hotelBranchId);
    public HotelBranchResponse activate(UUID hotelBranchId);
    public HotelBranchResponse deactivate(UUID hotelBranchId);
    public List<HotelBranchResponse> findByHotel(UUID hotelId);
}
