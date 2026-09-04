package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.enums.EStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.HotelBranchMapper;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.repository.HotelBranchRepo;
import rw.afriteck.pms.repository.HotelRepo;
import rw.afriteck.pms.service.IHotelBranchService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelBranchServiceImpl implements IHotelBranchService {

    private final HotelBranchRepo hotelBranchRepo;
    private final HotelRepo hotelRepo;
    private final HotelBranchMapper hotelBranchMapper;

    @Override
    public HotelBranchResponse create(CreateHotelBranchRequest request) {
        Hotel hotel = hotelRepo.findById(request.hotelId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel", request.hotelId()));
        HotelBranch hotelBranch = hotelBranchMapper.toEntity(request);
        hotelBranch.setHotel(hotel);
        hotelBranch.setStatus(EStatus.ACTIVE);

        HotelBranch saved = hotelBranchRepo.save(hotelBranch);
        return hotelBranchMapper.toResponse(saved);
    }

    @Override
    public List<HotelBranchResponse> findAll() {
        return hotelBranchRepo.findAll()
                .stream()
                .map(hotelBranchMapper::toResponse)
                .toList();
    }

    @Override
    public HotelBranchResponse findOne(UUID hotelBranchId) {
        HotelBranch hotelBranch = hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
        return hotelBranchMapper.toResponse(hotelBranch);
    }

    @Override
    public HotelBranchResponse activate(UUID hotelBranchId) {
        HotelBranch hotelBranch = hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
        hotelBranch.setStatus(EStatus.ACTIVE);
        HotelBranch saved = hotelBranchRepo.save(hotelBranch);
        return hotelBranchMapper.toResponse(saved);
    }

    @Override
    public HotelBranchResponse deactivate(UUID hotelBranchId) {
        HotelBranch hotelBranch = hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
        hotelBranch.setStatus(EStatus.INACTIVE);
        HotelBranch saved = hotelBranchRepo.save(hotelBranch);
        return hotelBranchMapper.toResponse(saved);
    }

    @Override
    public List<HotelBranchResponse> findByHotel(UUID hotelId) {
        return hotelBranchRepo.findByHotelHotelId(hotelId)
                .stream()
                .map(hotelBranchMapper::toResponse)
                .toList();
    }
}
