package rw.afriteck.pms.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.enums.ERecordStatus;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.restaurant.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.restaurant.dtos.MenuMasterResponse;
import rw.afriteck.pms.restaurant.mapper.MenuMasterMapper;
import rw.afriteck.pms.restaurant.model.MenuCategory;
import rw.afriteck.pms.restaurant.model.MenuMaster;
import rw.afriteck.pms.restaurant.repository.MenuCategoryRepo;
import rw.afriteck.pms.restaurant.repository.MenuMasterRepo;
import rw.afriteck.pms.restaurant.service.IMenuMasterService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuMasterServiceImpl implements IMenuMasterService {
    private final MenuMasterRepo menuMasterRepo;
    private final MenuMasterMapper menuMasterMapper;
    private final MenuCategoryRepo menuCategoryRepo;

    @Override
    @Transactional
    public MenuMasterResponse register(CreateMenuMasterRequest menuMasterRequest) {
        MenuCategory menuCategory = menuCategoryRepo.findById(menuMasterRequest.menuCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Menu Category", menuMasterRequest.menuCategoryId()));

        MenuMaster menuMaster = menuMasterMapper.toEntity(menuMasterRequest);
        menuMaster.setMenuCategory(menuCategory);
        menuMaster.setStatus(ERecordStatus.ACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    @Transactional
    public MenuMasterResponse update(UUID id, CreateMenuMasterRequest menuMasterRequest) {
        MenuCategory menuCategory = menuCategoryRepo.findById(menuMasterRequest.menuCategoryId())
                .orElseThrow(()->new ResourceNotFoundException("Menu Category", menuMasterRequest.menuCategoryId()));

        MenuMaster menuMaster = menuMasterRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Menu Master", id));

        menuMasterMapper.updateEntityFromRequest(menuMasterRequest, menuMaster);
        menuMaster.setMenuCategory(menuCategory);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuMasterResponse> findAll(Pageable pageable) {
        return menuMasterRepo.findAll(pageable)
                .map(menuMasterMapper::toResponse);
    }

    @Override
    public MenuMasterResponse findOne(UUID menuMasterId) {
        return menuMasterMapper.toResponse(menuMasterRepo.findById(menuMasterId).orElseThrow(()->new ResourceNotFoundException("Menu Master", menuMasterId)));
    }

    @Override
    @Transactional
    public MenuMasterResponse updateUnitPrice(UUID menuMasterId, BigDecimal unitPrice) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(()->new ResourceNotFoundException("Menu Master", menuMasterId));

        menuMaster.setUnitPrice(unitPrice);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    @Transactional(readOnly = true)
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
