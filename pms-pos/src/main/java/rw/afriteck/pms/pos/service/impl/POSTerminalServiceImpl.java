package rw.afriteck.pms.pos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.pos.dtos.*;
import rw.afriteck.pms.pos.mapper.POSProductCategoryMapper;
import rw.afriteck.pms.pos.mapper.POSTerminalMapper;
import rw.afriteck.pms.pos.model.POSProductCategory;
import rw.afriteck.pms.pos.model.POSTerminal;
import rw.afriteck.pms.pos.repository.POSProductCategoryRepository;
import rw.afriteck.pms.pos.repository.POSTerminalRepository;
import rw.afriteck.pms.pos.service.POSProductCategoryService;
import rw.afriteck.pms.pos.service.POSTerminalService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class POSTerminalServiceImpl implements POSTerminalService {

    private final POSTerminalRepository terminalRepo;
    private final POSTerminalMapper mapper;

    @Override
    @Transactional
    public POSTerminalResponse create(CreateTerminalRequest request) {
        if (terminalRepo.existsByHotelBranchIdAndCode(request.hotelBranchId(), request.code())) {
            throw new DuplicateResourceException("Terminal with Code:"+ request.code()+" already exists");
        }
        POSTerminal entity = mapper.toEntity(request);
        entity.setStatus(ERecordStatus.ACTIVE);
        terminalRepo.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public POSTerminalResponse findById(UUID id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<POSTerminalResponse> findByHotelBranch(UUID hotelBranchId) {
        return terminalRepo.findByHotelBranchId(hotelBranchId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public POSTerminalResponse activate(UUID id) {
        POSTerminal entity = getOrThrow(id);
        entity.setStatus(ERecordStatus.ACTIVE);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public POSTerminalResponse deactivate(UUID id) {
        POSTerminal entity = getOrThrow(id);
        entity.setStatus(ERecordStatus.INACTIVE);
        return mapper.toResponse(entity);
    }

    private POSTerminal getOrThrow(UUID id) {
        return terminalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("POSProductCategory", id));
    }
}
