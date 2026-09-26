package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import rw.afriteck.pms.restaurant.dtos.CreateMenuCategoryRequest;
import rw.afriteck.pms.restaurant.dtos.MenuCategoryResponse;
import rw.afriteck.pms.restaurant.model.MenuCategory;

@Mapper(componentModel = "spring")
public interface MenuCategoryMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    MenuCategoryResponse toResponse(MenuCategory menuCategory);
    MenuCategory toEntity(CreateMenuCategoryRequest menuCategoryRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    void updateEntityFromRequest(CreateMenuCategoryRequest request, @MappingTarget MenuCategory entity);

}
