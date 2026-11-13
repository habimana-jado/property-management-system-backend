package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.query.ItemSearch;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.api.view.PageResult;

import java.util.List;
import java.util.UUID;

public interface CatalogSetupApi {
    List<UnitView> units();
    UnitView createUnit(UnitCommand command);
    UnitView renameUnit(UUID id, String name);

    List<CategoryView> categories(boolean includeInactive);
    CategoryView createCategory(CategoryCommand command);
    CategoryView updateCategory(UUID id, CategoryUpdateCommand command);
    void setCategoryActive(UUID id, boolean active);

    PageResult<ItemView> searchItems(ItemSearch search, int page, int size);
    ItemView item(UUID id);
    ItemView createItem(ItemCommand command);
    ItemView updateItem(UUID id, ItemUpdateCommand command);
    void setItemActive(UUID id, boolean active);
    ItemView putPackaging(UUID itemId, PackagingCommand command);
    ItemView removePackaging(UUID itemId, UUID unitId);
}

