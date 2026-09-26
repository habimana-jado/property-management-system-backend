package rw.afriteck.pms.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rw.afriteck.pms.restaurant.dtos.CreateTableMasterRequest;
import rw.afriteck.pms.restaurant.dtos.TableMasterResponse;
import rw.afriteck.pms.restaurant.enums.ETableStatus;

import java.util.UUID;

public interface ITableMasterService {
    TableMasterResponse register(CreateTableMasterRequest tableMasterRequest);

    TableMasterResponse update(UUID id, CreateTableMasterRequest tableMasterRequest);

    Page<TableMasterResponse> findAll(Pageable pageable);

    TableMasterResponse findOne(UUID tableId);

    Page<TableMasterResponse> findByRestaurantAndActive(UUID restaurantId, Pageable pageable);

    TableMasterResponse changeStatus(UUID tableId, ETableStatus status);

    TableMasterResponse activate(UUID id);

    TableMasterResponse deactivate(UUID id);
}
