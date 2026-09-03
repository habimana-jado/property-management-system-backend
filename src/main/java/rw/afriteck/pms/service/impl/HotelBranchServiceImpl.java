package rw.afriteck.pms.service.impl;

import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.HotelBranchRequest;
import rw.afriteck.pms.enums.EStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.repository.HotelBranchRepo;
import rw.afriteck.pms.repository.HotelRepo;
import rw.afriteck.pms.service.IHotelBranchService;
import rw.afriteck.pms.service.IHotelService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HotelBranchServiceImpl implements IHotelBranchService {

    private final HotelBranchRepo hotelBranchRepo;
    private final IHotelService hotelService;
    private final ObjectMapper objectMapper;

    public HotelBranchServiceImpl(HotelBranchRepo hotelBranchRepo, IHotelService hotelService, ObjectMapper objectMapper){
        this.hotelBranchRepo=hotelBranchRepo;
        this.hotelService = hotelService;
        this.objectMapper = objectMapper;
    }

    @Override
    public HotelBranch registerHotelBranch(HotelBranchRequest hotelBranchRequest) {
        HotelBranch hotelBranch = this.objectMapper.convertValue(hotelBranchRequest, HotelBranch.class);
        Hotel hotel = this.hotelService.findOne(hotelBranchRequest.hotelId());
        hotelBranch.setHotel(hotel);
        hotelBranch.setStatus(EStatus.ACTIVE);
        return this.hotelBranchRepo.save(hotelBranch);
    }

    @Override
    public List<HotelBranch> findAll() {
        return this.hotelBranchRepo.findAll();
    }

    @Override
    public HotelBranch findOne(UUID hotelBranchId) {
        return this.hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
    }

    @Override
    public HotelBranch activate(UUID hotelBranchId) {
        HotelBranch hotelBranch = this.hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
        hotelBranch.setStatus(EStatus.ACTIVE);
        return this.hotelBranchRepo.save(hotelBranch);

    }

    @Override
    public HotelBranch deactivate(UUID hotelBranchId) {
        HotelBranch hotelBranch = this.hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
        hotelBranch.setStatus(EStatus.INACTIVE);
        return this.hotelBranchRepo.save(hotelBranch);
    }

    @Override
    public List<HotelBranch> findByHotel(UUID hotelId) {
        return this.hotelBranchRepo.findByHotelHotelId(hotelId);
    }
}
