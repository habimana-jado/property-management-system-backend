package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.HotelBranchRequest;
import rw.afriteck.pms.model.HotelBranch;

import java.util.List;
import java.util.UUID;

public interface IHotelBranchService {
    public HotelBranch registerHotelBranch(HotelBranchRequest hotelBranchRequest);
    public List<HotelBranch> findAll();
    public HotelBranch findOne(UUID hotelBranchId);
    public HotelBranch activate(UUID hotelBranchId);
    public HotelBranch deactivate(UUID hotelBranchId);
    public List<HotelBranch> findByHotel(UUID hotelId);
}
