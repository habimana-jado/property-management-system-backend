package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.dtos.CreateTableMasterRequest;
import rw.afriteck.pms.dtos.TableMasterResponse;
import rw.afriteck.pms.enums.ERecordStatus;
import rw.afriteck.pms.enums.ETableStatus;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.mapper.TableMasterMapper;
import rw.afriteck.pms.model.Restaurant;
import rw.afriteck.pms.model.TableMaster;
import rw.afriteck.pms.repository.RestaurantRepo;
import rw.afriteck.pms.repository.TableMasterRepo;
import rw.afriteck.pms.service.ITableMasterService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableMasterService implements ITableMasterService {

    private final TableMasterRepo tableMasterRepo;
    private final RestaurantRepo restaurantRepo;
    private final TableMasterMapper tableMasterMapper;

    @Override
    @Transactional
    public TableMasterResponse register(CreateTableMasterRequest tableMasterRequest) {
        Restaurant restaurant = restaurantRepo.findById(tableMasterRequest.restaurantId())
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", tableMasterRequest.restaurantId()));
        TableMaster tableMaster = tableMasterMapper.toEntity(tableMasterRequest);
        tableMaster.setTableStatus(ETableStatus.AVAILABLE);
        tableMaster.setRecordStatus(ERecordStatus.ACTIVE);
        tableMaster.setRestaurant(restaurant);
        TableMaster saved = tableMasterRepo.save(tableMaster);
        return tableMasterMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TableMasterResponse update(UUID id, CreateTableMasterRequest tableMasterRequest) {
        TableMaster tableMaster = tableMasterRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Table Master", id));
        Restaurant restaurant = restaurantRepo.findById(tableMasterRequest.restaurantId())
                .orElseThrow(()->new ResourceNotFoundException("Restaurant", tableMasterRequest.restaurantId()));

        tableMasterMapper.updateEntityFromRequest(tableMasterRequest, tableMaster);

        tableMaster.setRestaurant(restaurant);
        return tableMasterMapper.toResponse(tableMasterRepo.save(tableMaster));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TableMasterResponse> findAll(Pageable pageable) {
        return tableMasterRepo.findAll(pageable)
                .map(tableMasterMapper::toResponse);
    }

    @Override
    public TableMasterResponse findOne(UUID tableId) {
        return tableMasterMapper.toResponse(tableMasterRepo.findById(tableId)
                .orElseThrow(()->new ResourceNotFoundException("Table Master", tableId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TableMasterResponse> findByRestaurantAndActive(UUID restaurantId, Pageable pageable) {
        return tableMasterRepo.findByRestaurantIdAndRecordStatus(restaurantId, ERecordStatus.ACTIVE, pageable)
                .map(tableMasterMapper::toResponse);
    }

    @Override
    @Transactional
    public TableMasterResponse changeStatus(UUID tableId, ETableStatus status) {
        TableMaster tableMaster = tableMasterRepo.findById(tableId)
                .orElseThrow(()->new ResourceNotFoundException("Table Master", tableId));
        tableMaster.setTableStatus(status);
        TableMaster saved = tableMasterRepo.save(tableMaster);
        return tableMasterMapper.toResponse(saved);
    }

    @Override
    public TableMasterResponse activate(UUID id) {
        TableMaster tableMaster = tableMasterRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Table Master", id));
        tableMaster.setRecordStatus(ERecordStatus.ACTIVE);
        TableMaster saved = tableMasterRepo.save(tableMaster);
        return tableMasterMapper.toResponse(saved);
    }

    @Override
    public TableMasterResponse deactivate(UUID id) {
        TableMaster tableMaster = tableMasterRepo.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Table Master", id));
        tableMaster.setRecordStatus(ERecordStatus.INACTIVE);
        TableMaster saved = tableMasterRepo.save(tableMaster);
        return tableMasterMapper.toResponse(saved);
    }
}
