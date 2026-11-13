package rw.afriteck.pms.stock.internal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockNotFoundException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.RecipeSetupApi;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.api.view.PageResult;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class RecipeSetupServiceImpl implements RecipeSetupApi {

    private final RecipeRepository recipes;
    private final StockItemRepository items;
    private final UnitOfMeasureRepository units;
    private final UnitOfConversionService conversion;

    @PersistenceContext
    private EntityManager em;

    private record ResolvedLine(StockItem ingredient, UnitOfMeasure unit, BigDecimal quantity) {}

    @Override @Transactional(readOnly = true)
    public PageResult<RecipeSummary> recipes(String text, boolean activeOnly, int page, int size) {
        JpqlQuery q = new JpqlQuery();
        if (text != null && !text.isBlank()) q.and("lower(r.name) like :text", "text", "%" + text.trim().toLowerCase() + "%");
        if (activeOnly) q.and("r.active = true");

        int pageSize = Paging.size(size);
        int pageNo = Paging.page(page);
        TypedQuery<Long> count = em.createQuery("select count(r) from Recipe r" + q.where(), Long.class);
        q.bind(count);
        long total = count.getSingleResult();
        if (total == 0) return new PageResult<>(List.of(), pageNo, pageSize, 0);

        TypedQuery<RecipeSummary> data = em.createQuery("select new " + RecipeSummary.class.getName()
                + "(r.id, r.name, r.active) from Recipe r" + q.where() + " order by r.name, r.id", RecipeSummary.class);
        q.bind(data);
        data.setFirstResult(pageNo * pageSize);
        data.setMaxResults(pageSize);
        return new PageResult<>(data.getResultList(), pageNo, pageSize, total);
    }

    @Override @Transactional(readOnly = true)
    public RecipeView recipe(UUID id) {
        return view(load(id));
    }

    @Override
    public RecipeView createRecipe(RecipeCommand c) {
        List<ResolvedLine> lines = resolve(c);
        Recipe r = new Recipe();
        r.setName(c.name().trim());
        lines.forEach(l -> r.addLine(toLine(l)));
        return view(recipes.save(r));
    }

    /** Edits apply to future sales only: past movements are already in the ledger. */
    @Override
    public RecipeView updateRecipe(UUID id, RecipeCommand c) {
        Recipe r = load(id);
        List<ResolvedLine> lines = resolve(c);                      // validate everything before changing anything

        Map<UUID, RecipeLine> existing = r.getLines().stream()
                .collect(Collectors.toMap(l -> l.getIngredient().getId(), Function.identity()));
        Set<UUID> keep = new HashSet<>();
        for (ResolvedLine l : lines) {
            keep.add(l.ingredient().getId());
            RecipeLine line = existing.get(l.ingredient().getId());
            if (line == null) {
                r.addLine(toLine(l));
            } else {
                line.setQuantity(l.quantity());
                line.setUnit(l.unit());
            }
        }
        r.getLines().removeIf(l -> !keep.contains(l.getIngredient().getId()));   // orphanRemoval deletes them
        r.setName(c.name().trim());
        return view(r);
    }

    @Override
    public void setRecipeActive(UUID id, boolean active) {
        Recipe r = load(id);
        r.setActive(active);
    }

    // ---------------- helpers ----------------
    private List<ResolvedLine> resolve(RecipeCommand c) {
        Set<UUID> seen = new HashSet<>();
        List<ResolvedLine> result = new ArrayList<>();
        for (RecipeLineCommand lc : c.lines()) {
            require(seen.add(lc.ingredientId()), "An ingredient can appear only once in a recipe");
            StockItem ingredient = items.findById(lc.ingredientId())
                    .orElseThrow(() -> new StockNotFoundException("Ingredient not found"));
            require(ingredient.isActive(), "Ingredient is inactive: " + ingredient.getName());
            UnitOfMeasure unit = units.findById(lc.unitId())
                    .orElseThrow(() -> new StockNotFoundException("Unit not found"));
            BigDecimal inBase = conversion.toBase(ingredient, unit, lc.quantity());   // throws when not convertible
            require(inBase.signum() > 0, "Quantity rounds to zero for " + ingredient.getName());
            result.add(new ResolvedLine(ingredient, unit, lc.quantity()));
        }
        return result;
    }

    private static RecipeLine toLine(ResolvedLine l) {
        RecipeLine line = new RecipeLine();
        line.setIngredient(l.ingredient());
        line.setUnit(l.unit());
        line.setQuantity(l.quantity());
        return line;
    }

    private Recipe load(UUID id) {
        return recipes.findWithLinesById(id).orElseThrow(() -> new StockNotFoundException("Recipe not found"));
    }

    private RecipeView view(Recipe r) {
        return new RecipeView(r.getId(), r.getName(), r.isActive(), r.getLines().stream()
                .sorted(Comparator.comparing(l -> l.getIngredient().getName()))
                .map(l -> new RecipeLineView(l.getIngredient().getId(), l.getIngredient().getCode(),
                        l.getIngredient().getName(), l.getQuantity(), l.getUnit().getId(), l.getUnit().getCode()))
                .toList());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}
