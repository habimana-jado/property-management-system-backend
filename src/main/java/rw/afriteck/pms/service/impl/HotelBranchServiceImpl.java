package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.dtos.HotelBranchResponse;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.HotelBranchMapper;
import rw.afriteck.pms.model.Hotel;
import rw.afriteck.pms.model.HotelBranch;
import rw.afriteck.pms.repository.HotelBranchRepo;
import rw.afriteck.pms.repository.HotelRepo;
import rw.afriteck.pms.service.IHotelBranchService;

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
        hotelBranch.setStatus(ERecordStatus.ACTIVE);

        HotelBranch saved = hotelBranchRepo.save(hotelBranch);
        return hotelBranchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelBranchResponse> findAll(Pageable pageable) {
        return hotelBranchRepo.findAll(pageable)
                .map(hotelBranchMapper::toResponse);
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
        hotelBranch.setStatus(ERecordStatus.ACTIVE);
        HotelBranch saved = hotelBranchRepo.save(hotelBranch);
        return hotelBranchMapper.toResponse(saved);
    }

    @Override
    public HotelBranchResponse deactivate(UUID hotelBranchId) {
        HotelBranch hotelBranch = hotelBranchRepo.findById(hotelBranchId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", hotelBranchId));
        hotelBranch.setStatus(ERecordStatus.INACTIVE);
        HotelBranch saved = hotelBranchRepo.save(hotelBranch);
        return hotelBranchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelBranchResponse> findByHotelAndActive(UUID hotelId, Pageable pageable) {
        return hotelBranchRepo.findByHotelIdAndStatus(hotelId, ERecordStatus.ACTIVE, pageable)
                .map(hotelBranchMapper::toResponse);
    }
}
