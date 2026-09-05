package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.dtos.TableBillItemResponse;
import rw.afriteck.pms.model.TableBillItem;

@Mapper(componentModel = "spring", uses = MenuMasterMapper.class)
public interface TableBillItemMapper {

    @Mapping(target = "menuMaster", source = "menuMaster")
    TableBillItemResponse toResponse(TableBillItem restaurant);

}
