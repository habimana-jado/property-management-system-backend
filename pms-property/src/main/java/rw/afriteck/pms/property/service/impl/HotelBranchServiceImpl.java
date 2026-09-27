package rw.afriteck.pms.property.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.property.dtos.CreateHotelBranchRequest;
import rw.afriteck.pms.property.dtos.HotelBranchResponse;
import rw.afriteck.pms.property.mapper.HotelBranchMapper;
import rw.afriteck.pms.property.model.Hotel;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.property.repository.HotelBranchRepo;
import rw.afriteck.pms.property.repository.HotelRepo;
import rw.afriteck.pms.property.service.HotelBranchService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelBranchServiceImpl implements HotelBranchService {

    private final HotelBranchRepo hotelBranchRepo;
    private final HotelRepo hotelRepo;
    private final HotelBranchMapper hotelBranchMapper;

    @Override
    @Transactional
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
    @Transactional
    public HotelBranchResponse update(UUID id, CreateHotelBranchRequest hotelBranchRequest) {
        Hotel hotel = hotelRepo.findById(hotelBranchRequest.hotelId())
                .orElseThrow(()->new ResourceNotFoundException("Hotel", hotelBranchRequest.hotelId()));

        HotelBranch hotelBranch = hotelBranchRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel Branch", id));

        hotelBranchMapper.updateEntityFromRequest(hotelBranchRequest, hotelBranch);

        hotelBranch.setHotel(hotel);
        return hotelBranchMapper.toResponse(hotelBranchRepo.save(hotelBranch));
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
