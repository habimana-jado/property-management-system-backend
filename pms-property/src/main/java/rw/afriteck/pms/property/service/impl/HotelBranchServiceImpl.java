package rw.afriteck.pms.property.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.common.tenant.TenantContext;
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
    private final HotelBranchMapper hotelBranchMapper;

    @Override
    @Transactional
    public HotelBranchResponse create(CreateHotelBranchRequest request) {
        TenantContext.require(); // fail early with a clear error if no hotel is selected
        HotelBranch branch = hotelBranchMapper.toEntity(request);
        branch.setStatus(ERecordStatus.ACTIVE);
        return hotelBranchMapper.toResponse(hotelBranchRepo.save(branch));
    }

    @Override
    @Transactional
    public HotelBranchResponse update(UUID id, CreateHotelBranchRequest request) {
        HotelBranch branch = hotelBranchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Branch", id));
        hotelBranchMapper.updateEntityFromRequest(request, branch);
        return hotelBranchMapper.toResponse(branch); // managed entity: dirty checking persists it
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelBranchResponse> findAll(Pageable pageable) {
        return hotelBranchRepo.findAll(pageable).map(hotelBranchMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelBranchResponse findOne(UUID id) {
        return hotelBranchMapper.toResponse(hotelBranchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Branch", id)));
    }

    @Override
    @Transactional
    public HotelBranchResponse activate(UUID id) {
        HotelBranch branch = hotelBranchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Branch", id));
        branch.setStatus(ERecordStatus.ACTIVE);
        return hotelBranchMapper.toResponse(branch);
    }

    @Override
    @Transactional
    public HotelBranchResponse deactivate(UUID id) {
        HotelBranch branch = hotelBranchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel Branch", id));
        branch.setStatus(ERecordStatus.INACTIVE);
        return hotelBranchMapper.toResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelBranchResponse> findByHotelAndActive(UUID hotelId, Pageable pageable) {
        TenantContext.requireCurrent(hotelId); // the path id must be your own hotel
        return hotelBranchRepo.findByStatus(ERecordStatus.ACTIVE, pageable)
                .map(hotelBranchMapper::toResponse);
    }
}
