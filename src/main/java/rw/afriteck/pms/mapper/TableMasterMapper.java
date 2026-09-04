package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.dtos.CreateTableMasterRequest;
import rw.afriteck.pms.dtos.TableMasterResponse;
import rw.afriteck.pms.model.TableMaster;

@Mapper(componentModel = "spring")
public interface TableMasterMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    TableMasterResponse toResponse(TableMaster tableMaster);
    TableMaster toEntity(CreateTableMasterRequest tableMasterRequest);
}
