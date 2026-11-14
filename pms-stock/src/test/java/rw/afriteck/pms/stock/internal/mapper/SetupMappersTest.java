package rw.afriteck.pms.stock.internal.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.enums.EItemType;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.api.enums.EUnitGroup;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.internal.model.*;

/** Wires only the generated mapper implementations: no database, no full application context. */
@SpringJUnitConfig(classes = {UnitMapperImpl.class, CategoryMapperImpl.class, PackagingMapperImpl.class,
        ItemMapperImpl.class, RecipeMapperImpl.class, StoreMapperImpl.class, AssignmentMapperImpl.class})
class SetupMappersTest {

    @Autowired UnitMapper unitMapper;
    @Autowired CategoryMapper categoryMapper;
    @Autowired ItemMapper itemMapper;
    @Autowired RecipeMapper recipeMapper;
    @Autowired StoreMapper storeMapper;
    @Autowired AssignmentMapper assignmentMapper;

    @Test
    void unit_mapsEveryField() {
        UnitOfMeasure kg = unit("KG", EUnitGroup.WEIGHT, "1000");

        UnitView v = unitMapper.toView(kg);

        assertThat(v.id()).isEqualTo(kg.getId());
        assertThat(v.code()).isEqualTo("KG");
        assertThat(v.unitGroup()).isEqualTo(EUnitGroup.WEIGHT);
        assertThat(v.factorToGroupBase()).isEqualByComparingTo("1000");
    }

    @Test
    void category_rootHasNoParentId_childHasTheParentId() {
        StockCategory root = category("BEV", null);
        StockCategory child = category("SOFT", root);

        assertThat(categoryMapper.toView(root).parentId()).isNull();
        assertThat(categoryMapper.toView(child).parentId()).isEqualTo(root.getId());
        assertThat(categoryMapper.toView(child).active()).isTrue();
    }

    @Test
    void item_mapsNestedCategoryUnitAndPackagings() {
        UnitOfMeasure pcs = unit("PCS", EUnitGroup.COUNT, "1");
        UnitOfMeasure carton = unit("CARTON", EUnitGroup.COUNT, "1");
        StockCategory cat = category("BEV", null);

        StockItem soda = new StockItem();
        ReflectionTestUtils.setField(soda, "id", UUID.randomUUID());
        soda.setCode("SODA");
        soda.setName("Soda");
        soda.setDescription("330ml can");
        soda.setCategory(cat);
        soda.setBaseUnit(pcs);
        soda.setItemType(EItemType.SELLABLE);
        soda.setReorderLevel(new BigDecimal("12"));

        ItemPackaging pack = new ItemPackaging();
        pack.setItem(soda);
        pack.setUnit(carton);
        pack.setQuantityInBaseUnit(new BigDecimal("24"));

        ItemView v = itemMapper.toView(soda, List.of(pack));

        assertThat(v.id()).isEqualTo(soda.getId());
        assertThat(v.code()).isEqualTo("SODA");
        assertThat(v.description()).isEqualTo("330ml can");
        assertThat(v.categoryId()).isEqualTo(cat.getId());
        assertThat(v.categoryName()).isEqualTo("BEV-name");
        assertThat(v.baseUnitId()).isEqualTo(pcs.getId());
        assertThat(v.baseUnitCode()).isEqualTo("PCS");
        assertThat(v.itemType()).isEqualTo(EItemType.SELLABLE);
        assertThat(v.reorderLevel()).isEqualByComparingTo("12");
        assertThat(v.active()).isTrue();
        assertThat(v.packagings()).hasSize(1);
        assertThat(v.packagings().get(0).unitCode()).isEqualTo("CARTON");
        assertThat(v.packagings().get(0).quantityInBaseUnit()).isEqualByComparingTo("24");
    }

    @Test
    void item_withNoPackagings_hasAnEmptyList() {
        StockItem soda = new StockItem();
        ReflectionTestUtils.setField(soda, "id", UUID.randomUUID());
        soda.setCode("SODA");
        soda.setName("Soda");
        soda.setCategory(category("BEV", null));
        soda.setBaseUnit(unit("PCS", EUnitGroup.COUNT, "1"));
        soda.setItemType(EItemType.SELLABLE);

        assertThat(itemMapper.toView(soda, List.of()).packagings()).isEmpty();
    }

    @Test
    void recipe_linesComeBackOrderedByIngredientName() {
        UnitOfMeasure g = unit("G", EUnitGroup.WEIGHT, "1");
        Recipe r = new Recipe();
        ReflectionTestUtils.setField(r, "id", UUID.randomUUID());
        r.setName("Burger");
        r.addLine(line(ingredient("SAUCE", "Sauce", g), g, "20"));
        r.addLine(line(ingredient("BEEF", "Beef", g), g, "150"));
        r.addLine(line(ingredient("CHEESE", "Cheese", g), g, "30"));

        RecipeView v = recipeMapper.toView(r);

        assertThat(v.name()).isEqualTo("Burger");
        assertThat(v.lines()).extracting(RecipeLineView::ingredientName).containsExactly("Beef", "Cheese", "Sauce");
        assertThat(v.lines().get(0).ingredientCode()).isEqualTo("BEEF");
        assertThat(v.lines().get(0).unitCode()).isEqualTo("G");
        assertThat(v.lines().get(0).quantity()).isEqualByComparingTo("150");
    }

    @Test
    void store_mapsTheBranchId() {
        HotelBranch branch = new HotelBranch();
        ReflectionTestUtils.setField(branch, "id", UUID.randomUUID());
        Store s = new Store();
        ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
        s.setBranch(branch);
        s.setCode("MAIN");
        s.setName("Main store");
        s.setStoreType(EStoreType.MAIN);

        StoreView v = storeMapper.toView(s);

        assertThat(v.branchId()).isEqualTo(branch.getId());
        assertThat(v.storeType()).isEqualTo(EStoreType.MAIN);
        assertThat(v.active()).isTrue();
    }

    @Test
    void assignment_mapsTheDefaultFlagAndStoreDetails() {
        Store s = new Store();
        ReflectionTestUtils.setField(s, "id", UUID.randomUUID());
        s.setName("Bar store");
        UUID outletId = UUID.randomUUID();

        StoreAssignment def = assignment(s, outletId, true);
        StoreAssignment other = assignment(s, outletId, false);

        AssignmentView dv = assignmentMapper.toView(def);
        assertThat(dv.defaultStore()).isTrue();
        assertThat(dv.storeId()).isEqualTo(s.getId());
        assertThat(dv.storeName()).isEqualTo("Bar store");
        assertThat(dv.outletType()).isEqualTo(EOutletType.RESTAURANT);
        assertThat(dv.outletId()).isEqualTo(outletId);
        assertThat(assignmentMapper.toView(other).defaultStore()).isFalse();
    }

    // ---- builders ----
    private static UnitOfMeasure unit(String code, EUnitGroup group, String factor) {
        UnitOfMeasure u = new UnitOfMeasure();
        ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
        u.setCode(code);
        u.setName(code);
        u.setUnitGroup(group);
        u.setFactorToGroupBase(new BigDecimal(factor));
        return u;
    }

    private static StockCategory category(String code, StockCategory parent) {
        StockCategory c = new StockCategory();
        ReflectionTestUtils.setField(c, "id", UUID.randomUUID());
        c.setCode(code);
        c.setName(code + "-name");
        c.setParent(parent);
        return c;
    }

    private static StockItem ingredient(String code, String name, UnitOfMeasure base) {
        StockItem i = new StockItem();
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        i.setCode(code);
        i.setName(name);
        i.setBaseUnit(base);
        return i;
    }

    private static RecipeLine line(StockItem ingredient, UnitOfMeasure unit, String qty) {
        RecipeLine l = new RecipeLine();
        l.setIngredient(ingredient);
        l.setUnit(unit);
        l.setQuantity(new BigDecimal(qty));
        return l;
    }

    private static StoreAssignment assignment(Store store, UUID outletId, boolean isDefault) {
        StoreAssignment a = new StoreAssignment();
        ReflectionTestUtils.setField(a, "id", UUID.randomUUID());
        a.setStore(store);
        a.setOutletType(EOutletType.RESTAURANT);
        a.setOutletId(outletId);
        a.setDefault(isDefault);
        a.setActive(true);
        return a;
    }
}