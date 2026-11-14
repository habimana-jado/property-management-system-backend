package rw.afriteck.pms.stock.internal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockNotFoundException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.CatalogSetupApi;
import rw.afriteck.pms.stock.api.enums.EUnitGroup;
import rw.afriteck.pms.stock.api.query.ItemSearch;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.api.view.PageResult;
import rw.afriteck.pms.stock.internal.mapper.CategoryMapper;
import rw.afriteck.pms.stock.internal.mapper.ItemMapper;
import rw.afriteck.pms.stock.internal.mapper.UnitMapper;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;
import rw.afriteck.pms.stock.internal.model.StockCategory;
import rw.afriteck.pms.stock.internal.model.StockItem;
import rw.afriteck.pms.stock.internal.model.UnitOfMeasure;
import rw.afriteck.pms.stock.internal.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class CatalogSetupServiceImpl implements CatalogSetupApi {

    private final UnitOfMeasureRepository units;
    private final StockCategoryRepository categories;
    private final StockItemRepository items;
    private final ItemPackagingRepository packagings;
    private final StockMovementRepository movements;
    private final StockBalanceRepository balances;
    private final RecipeLineRepository recipeLines;
    private final UnitMapper unitMapper;
    private final CategoryMapper categoryMapper;
    private final ItemMapper itemMapper;

    @PersistenceContext
    private EntityManager em;

    // ---------------- units ----------------
    @Override @Transactional(readOnly = true)
    public List<UnitView> units() {
        return units.findAll(Sort.by("unitGroup", "code")).stream().map(unitMapper::toView).toList();
    }

    @Override
    public UnitView createUnit(UnitCommand c) {
        String code = Codes.normalize(c.code());
        require(!units.existsByCode(code), "Unit code already exists: " + code);
        if (c.unitGroup() == EUnitGroup.COUNT) {
            require(c.factorToGroupBase().compareTo(BigDecimal.ONE) == 0,
                    "Count units have a factor of 1; what a carton or bottle contains is set per item as packaging");
        }
        UnitOfMeasure u = new UnitOfMeasure();
        u.setCode(code);
        u.setName(c.name().trim());
        u.setUnitGroup(c.unitGroup());
        u.setFactorToGroupBase(c.factorToGroupBase());
        return unitMapper.toView(units.save(u));
    }

    /** Only the name can change: group and factor are baked into every stored quantity. */
    @Override
    public UnitView renameUnit(UUID id, String name) {
        UnitOfMeasure u = unit(id);
        u.setName(name.trim());
        return unitMapper.toView(u);
    }

    // ---------------- categories ----------------
    @Override @Transactional(readOnly = true)
    public List<CategoryView> categories(boolean includeInactive) {
        return categories.findAll(Sort.by("name")).stream()
                .filter(c -> includeInactive || c.isActive()).map(categoryMapper::toView).toList();
    }

    @Override
    public CategoryView createCategory(CategoryCommand c) {
        String code = Codes.normalize(c.code());
        require(!categories.existsByCode(code), "Category code already exists: " + code);
        StockCategory cat = new StockCategory();
        cat.setCode(code);
        cat.setName(c.name().trim());
        cat.setParent(c.parentId() == null ? null : activeCategory(c.parentId()));
        return categoryMapper.toView(categories.save(cat));
    }

    @Override
    public CategoryView updateCategory(UUID id, CategoryUpdateCommand c) {
        StockCategory cat = category(id);
        StockCategory parent = c.parentId() == null ? null : activeCategory(c.parentId());
        for (StockCategory p = parent; p != null; p = p.getParent()) {          // no cycles
            require(!p.getId().equals(cat.getId()), "A category cannot be its own ancestor");
        }
        cat.setName(c.name().trim());
        cat.setParent(parent);
        return categoryMapper.toView(cat);
    }

    @Override
    public void setCategoryActive(UUID id, boolean active) {
        category(id).setActive(active);
    }

    // ---------------- items ----------------
    @Override @Transactional(readOnly = true)
    public PageResult<ItemView> searchItems(ItemSearch s, int page, int size) {
        JpqlQuery q = new JpqlQuery();
        if (s.text() != null && !s.text().isBlank()) {
            q.and("(lower(i.name) like :text or lower(i.code) like :text)",
                    "text", "%" + s.text().trim().toLowerCase() + "%");
        }
        if (s.categoryId() != null) q.and("i.category.id = :categoryId", "categoryId", s.categoryId());
        if (s.type() != null)       q.and("i.itemType = :itemType", "itemType", s.type());
        if (s.activeOnly())         q.and("i.active = true");

        int pageSize = Paging.size(size);
        int pageNo = Paging.page(page);

        TypedQuery<Long> count = em.createQuery("select count(i) from StockItem i" + q.where(), Long.class);
        q.bind(count);
        long total = count.getSingleResult();
        if (total == 0) return new PageResult<>(List.of(), pageNo, pageSize, 0);

        TypedQuery<StockItem> data = em.createQuery(
                "select i from StockItem i join fetch i.category join fetch i.baseUnit"
                        + q.where() + " order by i.name, i.id", StockItem.class);
        q.bind(data);
        data.setFirstResult(pageNo * pageSize);
        data.setMaxResults(pageSize);
        List<StockItem> found = data.getResultList();

        Map<UUID, List<ItemPackaging>> packagingByItem = packagings
                .findByItemIdIn(found.stream().map(StockItem::getId).toList()).stream()
                .collect(Collectors.groupingBy(p -> p.getItem().getId()));

        return new PageResult<>(found.stream()
                .map(i -> itemMapper.toView(i, packagingByItem.getOrDefault(i.getId(), List.of()))).toList(),
                pageNo, pageSize, total);
    }

    @Override @Transactional(readOnly = true)
    public ItemView item(UUID id) {
        return viewWithPackagings(item_(id));
    }

    @Override
    public ItemView createItem(ItemCommand c) {
        String code = Codes.normalize(c.code());
        require(!items.existsByCode(code), "Item code already exists: " + code);
        StockItem i = new StockItem();
        i.setCode(code);
        i.setName(c.name().trim());
        i.setDescription(c.description());
        i.setCategory(activeCategory(c.categoryId()));
        i.setBaseUnit(unit(c.baseUnitId()));
        i.setItemType(c.itemType());
        i.setReorderLevel(c.reorderLevel());
        return itemMapper.toView(items.save(i), List.of());
    }

    @Override
    public ItemView updateItem(UUID id, ItemUpdateCommand c) {
        StockItem i = item_(id);
        UnitOfMeasure newBase = unit(c.baseUnitId());
        if (!newBase.getId().equals(i.getBaseUnit().getId())) {
            // every stored quantity is in the old unit: changing it would silently corrupt history
            require(!movements.existsByItemId(id) && !balances.existsByItemId(id)
                            && !packagings.existsByItemId(id) && !recipeLines.existsByIngredientId(id),
                    "The base unit cannot change once the item has stock history, packaging or recipe use. "
                            + "Create a new item instead.");
            i.setBaseUnit(newBase);
        }
        if (!i.getCategory().getId().equals(c.categoryId())) i.setCategory(activeCategory(c.categoryId()));
        i.setName(c.name().trim());
        i.setDescription(c.description());
        i.setItemType(c.itemType());
        i.setReorderLevel(c.reorderLevel());
        return viewWithPackagings(i);
    }

    @Override
    public void setItemActive(UUID id, boolean active) {
        item_(id).setActive(active);
    }

    @Override
    public ItemView putPackaging(UUID itemId, PackagingCommand c) {
        StockItem i = item_(itemId);
        UnitOfMeasure u = unit(c.unitId());
        require(u.getUnitGroup() == EUnitGroup.COUNT, "Packaging units must be count units (carton, crate, bottle...)");
        require(!u.getId().equals(i.getBaseUnit().getId()), "The packaging unit is the item's base unit");

        ItemPackaging p = packagings.findByItemIdAndUnitId(itemId, u.getId()).orElse(null);
        if (p == null) {
            p = new ItemPackaging();
            p.setItem(i);
            p.setUnit(u);
        } else {
            // a recipe line "1 BOTTLE" would silently change meaning
            require(p.getQuantityInBaseUnit().compareTo(c.quantityInBaseUnit()) == 0
                            || !recipeLines.existsByIngredientIdAndUnitId(itemId, u.getId()),
                    "A recipe uses this packaging: edit the recipe first");
        }
        p.setQuantityInBaseUnit(c.quantityInBaseUnit());
        packagings.save(p);
        return viewWithPackagings(i);
    }

    @Override
    public ItemView removePackaging(UUID itemId, UUID unitId) {
        StockItem i = item_(itemId);
        ItemPackaging p = packagings.findByItemIdAndUnitId(itemId, unitId)
                .orElseThrow(() -> new StockNotFoundException("Packaging not found"));
        require(!recipeLines.existsByIngredientIdAndUnitId(itemId, unitId),
                "A recipe uses this packaging: edit the recipe first");
        packagings.delete(p);
        return viewWithPackagings(i);
    }

    // ---------------- helpers ----------------
    private UnitOfMeasure unit(UUID id) {
        return units.findById(id).orElseThrow(() -> new StockNotFoundException("Unit not found"));
    }
    private StockCategory category(UUID id) {
        return categories.findById(id).orElseThrow(() -> new StockNotFoundException("Category not found"));
    }
    private StockCategory activeCategory(UUID id) {
        StockCategory c = category(id);
        require(c.isActive(), "Category is inactive: " + c.getName());
        return c;
    }
    private StockItem item_(UUID id) {
        return items.findById(id).orElseThrow(() -> new StockNotFoundException("Item not found"));
    }

    private ItemView viewWithPackagings(StockItem i) {
        return itemMapper.toView(i, packagings.findByItemId(i.getId()));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}