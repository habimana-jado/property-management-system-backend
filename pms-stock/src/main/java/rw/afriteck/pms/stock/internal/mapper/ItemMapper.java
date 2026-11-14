package rw.afriteck.pms.stock.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import rw.afriteck.pms.stock.api.setup.ItemView;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;
import rw.afriteck.pms.stock.internal.model.StockItem;

import java.util.List;

/**
 * Two source parameters, so every mapping is explicit. Packagings are not reachable from the item,
 * so the caller loads them and passes them in (one query for a whole page of items).
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PackagingMapper.class)
public interface ItemMapper {

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "code", source = "item.code")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "description", source = "item.description")
    @Mapping(target = "categoryId", source = "item.category.id")
    @Mapping(target = "categoryName", source = "item.category.name")
    @Mapping(target = "baseUnitId", source = "item.baseUnit.id")
    @Mapping(target = "baseUnitCode", source = "item.baseUnit.code")
    @Mapping(target = "itemType", source = "item.itemType")
    @Mapping(target = "reorderLevel", source = "item.reorderLevel")
    @Mapping(target = "active", source = "item.active")
    @Mapping(target = "packagings", source = "packagings")
    ItemView toView(StockItem item, List<ItemPackaging> packagings);
}