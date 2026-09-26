package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.restaurant.dtos.CreateMenuMasterRequest;
import rw.afriteck.pms.restaurant.dtos.MenuItemSearchResponse;
import rw.afriteck.pms.restaurant.dtos.MenuMasterResponse;
import rw.afriteck.pms.restaurant.dtos.MenuMasterSummary;
import rw.afriteck.pms.restaurant.model.MenuMaster;

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
