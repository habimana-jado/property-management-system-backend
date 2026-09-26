package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.restaurant.dtos.CreateTableMasterRequest;
import rw.afriteck.pms.restaurant.dtos.TableMasterResponse;
import rw.afriteck.pms.restaurant.model.TableMaster;

@Mapper(componentModel = "spring")
public interface TableMasterMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    TableMasterResponse toResponse(TableMaster tableMaster);

    TableMaster toEntity(CreateTableMasterRequest tableMasterRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    void updateEntityFromRequest(CreateTableMasterRequest request, @MappingTarget TableMaster entity);

}
