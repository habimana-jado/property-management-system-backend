package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import rw.afriteck.pms.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.dtos.MenuMasterResponse;
import rw.afriteck.pms.model.MenuMaster;

@Mapper(componentModel = "spring")
public interface MenuMasterMapper {

    @Mapping(target = "menuCategoryId", source = "menuCategory.id")
    MenuMasterResponse toResponse(MenuMaster menuMaster);
    MenuMaster toEntity(CreateMenuMasterRequest menuMasterRequest);
}
