package rw.afriteck.pms.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.dtos.*;
import rw.afriteck.pms.model.MenuMaster;

@Mapper(componentModel = "spring")
public interface MenuMasterMapper {

    @Mapping(target = "menuCategoryId", source = "menuCategory.id")
    MenuMasterResponse toResponse(MenuMaster menuMaster);

    MenuMaster toEntity(CreateMenuMasterRequest menuMasterRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "menuCategory", ignore = true)
    void updateEntityFromRequest(CreateMenuMasterRequest request, @MappingTarget MenuMaster entity);

    MenuMasterSummary toSummary(MenuMaster menuMaster);

    @Mapping(source = "menuItemName", target = "name")
    MenuItemSearchResponse toSearchResponse(MenuMaster menuMaster);
}
