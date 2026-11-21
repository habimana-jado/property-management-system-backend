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
import rw.afriteck.pms.restaurant.service.MenuMasterService;
import rw.afriteck.pms.stock.api.StockCatalogApi;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuMasterServiceImpl implements MenuMasterService {
    private final MenuMasterRepo menuMasterRepo;
    private final MenuMasterMapper menuMasterMapper;
    private final MenuCategoryRepo menuCategoryRepo;
    private final StockCatalogApi stockCatalogApi;
    private final RestaurantBranchAccess branchAccess;

    @Override
    @Transactional
    public MenuMasterResponse register(CreateMenuMasterRequest menuMasterRequest) {
        stockCatalogApi.validateSaleMapping(menuMasterRequest.trackInventory(), menuMasterRequest.stockItemId(), menuMasterRequest.recipeId(), true);

        MenuCategory menuCategory = menuCategoryRepo.findById(menuMasterRequest.menuCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu Category", menuMasterRequest.menuCategoryId()));

        branchAccess.require(menuCategory.getRestaurant());

        MenuMaster menuMaster = menuMasterMapper.toEntity(menuMasterRequest);
        menuMaster.setMenuCategory(menuCategory);
        menuMaster.setStatus(ERecordStatus.ACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    @Transactional
    public MenuMasterResponse update(UUID id, CreateMenuMasterRequest menuMasterRequest) {
        stockCatalogApi.validateSaleMapping(menuMasterRequest.trackInventory(), menuMasterRequest.stockItemId(), menuMasterRequest.recipeId(), true);
        MenuCategory menuCategory = menuCategoryRepo.findById(menuMasterRequest.menuCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu Category", menuMasterRequest.menuCategoryId()));

        branchAccess.require(menuCategory.getRestaurant());

        MenuMaster menuMaster = menuMasterRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Master", id));

        branchAccess.require(menuMaster.getMenuCategory().getRestaurant());

        menuMasterMapper.updateEntityFromRequest(menuMasterRequest, menuMaster);
        menuMaster.setMenuCategory(menuCategory);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MenuMasterResponse> findAll(Pageable pageable) {
        return menuMasterRepo.findAll(pageable)
                .map(menuMasterMapper::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MenuMasterResponse> findByHotelBranchId(UUID hotelBranchId, Pageable pageable) {
        return menuMasterRepo.findByMenuCategoryRestaurantHotelBranchId(hotelBranchId, pageable)
                .map(menuMasterMapper::toResponse);
    }

    @Override
    public MenuMasterResponse findOne(UUID menuMasterId) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Master", menuMasterId));
        branchAccess.require(menuMaster.getMenuCategory().getRestaurant());
        return menuMasterMapper.toResponse(menuMaster);
    }

    @Override
    @Transactional
    public MenuMasterResponse updateUnitPrice(UUID menuMasterId, BigDecimal unitPrice) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Master", menuMasterId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Menu Master", menuMasterId));

        branchAccess.require(menuMaster.getMenuCategory().getRestaurant());

        menuMaster.setStatus(ERecordStatus.ACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }

    @Override
    public MenuMasterResponse deactivate(UUID menuMasterId) {
        MenuMaster menuMaster = menuMasterRepo.findById(menuMasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Master", menuMasterId));

        branchAccess.require(menuMaster.getMenuCategory().getRestaurant());

        menuMaster.setStatus(ERecordStatus.INACTIVE);
        return menuMasterMapper.toResponse(menuMasterRepo.save(menuMaster));
    }
}
