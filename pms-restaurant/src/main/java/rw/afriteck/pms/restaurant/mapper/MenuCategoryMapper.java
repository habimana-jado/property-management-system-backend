package rw.afriteck.pms.restaurant.mapper;

import org.mapstruct.*;
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
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CreateMenuCategoryRequest request, @MappingTarget MenuCategory entity);

}
