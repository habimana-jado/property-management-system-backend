package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.CategoryView;
import rw.afriteck.pms.stock.internal.model.StockCategory;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryView toView(StockCategory category);
}