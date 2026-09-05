package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rw.afriteck.pms.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.dtos.MenuMasterResponse;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.MenuMasterMapper;
import rw.afriteck.pms.model.MenuCategory;
import rw.afriteck.pms.model.MenuMaster;
import rw.afriteck.pms.repository.MenuCategoryRepo;
import rw.afriteck.pms.repository.MenuMasterRepo;
import rw.afriteck.pms.service.IMenuMasterService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuMasterService implements IMenuMasterService {
    private final MenuMasterRepo menuMasterRepo;
    private final MenuMasterMapper menuMasterMapper;
    private final MenuCategoryRepo menuCategoryRepo;

    @Override
    public MenuMasterResponse register(CreateMenuMasterRequest menuMasterRequest) {
        MenuCategory menuCategory = menuCategoryRepo.findById(menuMasterRequest.menuCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Menu Category", menuMasterRequest.menuCategoryId()));

        MenuMaster menuMaster = menuMasterMapper.toEntity(menuMasterRequest);
        menuMaster.setMenuCategory(menuCategory);
        menuMaster.setStatus(ERecordStatus.ACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    public Page<MenuMasterResponse> findAll(Pageable pageable) {
        return menuMasterRepo.findAll(pageable)
                .map(menuMasterMapper::toResponse);
    }

    @Override
    public MenuMasterResponse findOne(UUID menuMasterId) {
        return menuMasterMapper.toResponse(menuMasterRepo.findById(menuMasterId).orElseThrow(()->new ResourceNotFoundException("Menu Master", menuMasterId)));
    }

    @Override
    public MenuMasterResponse updateUnitPrice(UUID menuMasterId, BigDecimal unitPrice) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(()->new ResourceNotFoundException("Menu Master", menuMasterId));

        menuMaster.setUnitPrice(unitPrice);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    public Page<MenuMasterResponse> findByRestaurantAndActive(UUID restaurantId, Pageable pageable) {
        return menuMasterRepo.findByMenuCategoryRestaurantId(restaurantId, pageable)
                .map(menuMasterMapper::toResponse);
    }

    @Override
    public MenuMasterResponse activate(UUID menuMasterId) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(()->new ResourceNotFoundException("Menu Master", menuMasterId));

        menuMaster.setStatus(ERecordStatus.ACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    public MenuMasterResponse deactivate(UUID menuMasterId) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(()->new ResourceNotFoundException("Menu Master", menuMasterId));

        menuMaster.setStatus(ERecordStatus.INACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }
}
