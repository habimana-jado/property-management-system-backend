package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.StockApi;
import rw.afriteck.pms.stock.api.command.ConsumeForSaleCommand;
import rw.afriteck.pms.stock.api.command.ReverseConsumptionCommand;
import rw.afriteck.pms.stock.api.command.SaleLine;
import rw.afriteck.pms.stock.internal.dtos.BalanceKey;
import rw.afriteck.pms.stock.internal.dtos.MovementContext;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.RecipeRepository;
import rw.afriteck.pms.stock.internal.repository.StockItemRepository;
import rw.afriteck.pms.stock.internal.repository.StockMovementRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class StockApiImpl implements StockApi {

    private final OutletStoreResolver storeResolver;
    private final StockLedger ledger;
    private final StockMovementRepository movements;
    private final StockItemRepository items;
    private final RecipeRepository recipes;
    private final UnitOfConversionService conversion;

    private record Requirement(StockItem item, BigDecimal quantity, UUID sourceLineId) {}

    private record Catalog(Map<UUID, StockItem> items, Map<UUID, Recipe> recipes) {}
    @Override
    @Transactional
    public List<UUID> consumeForSale(ConsumeForSaleCommand cmd) {

        List<SaleLine> tracked = cmd.lines().stream().filter(SaleLine::trackInventory).toList();
        if (tracked.isEmpty()) return List.of();

        tracked.forEach(this::validateShape);

        Store store = storeResolver.resolveIssuingStore(cmd.outletType(), cmd.outletId(), cmd.storeId());

        Catalog catalog = loadCatalog(tracked);

        List<Requirement> requirements = new ArrayList<>();
        for (SaleLine line : tracked) {
            requirements.addAll(expand(line, catalog));
        }

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(requirements.stream()
                .map(r -> new BalanceKey(store.getId(), r.item().getId())).toList());

        List<UUID> ids = new ArrayList<>();
        for (Requirement r : requirements) {
            StockBalance balance = locked.get(new BalanceKey(store.getId(), r.item().getId()));
            ids.add(ledger.post(balance, EMovementType.SALE, r.quantity().negate(),
                    MovementContext.forSale(cmd.sourceType(), cmd.sourceId(), r.sourceLineId(),
                            cmd.performedBy())).getId());
        }
        return ids;
    }

    private void validateShape(SaleLine line) {
        if ((line.stockItemId() == null) == (line.recipeId() == null)) {
            throw new StockRuleViolationException("A sale line needs exactly one of stockItemId or recipeId");
        }
        if (line.quantity() == null || line.quantity().signum() <= 0) {
            throw new StockRuleViolationException("Sale quantity must be positive");
        }
    }

    private Catalog loadCatalog(List<SaleLine> lines) {
        List<UUID> itemIds = lines.stream().map(SaleLine::stockItemId)
                .filter(Objects::nonNull).distinct().toList();
        List<UUID> recipeIds = lines.stream().map(SaleLine::recipeId)
                .filter(Objects::nonNull).distinct().toList();

        // guard the empty case: no query, and no empty "in ()" clause
        Map<UUID, StockItem> itemById = itemIds.isEmpty() ? Map.of()
                : items.findAllById(itemIds).stream()
                .collect(Collectors.toMap(StockItem::getId, Function.identity()));
        Map<UUID, Recipe> recipeById = recipeIds.isEmpty() ? Map.of()
                : recipes.findWithLinesByIdIn(recipeIds).stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        return new Catalog(itemById, recipeById);
    }

    private List<Requirement> expand(SaleLine line, Catalog catalog) {
        if (line.stockItemId() != null) {
            StockItem item = catalog.items().get(line.stockItemId());
            if (item == null) throw new StockRuleViolationException("Unknown stock item");
            requireActive(item);
            return List.of(new Requirement(item, line.quantity(), line.sourceLineId()));
        }

        Recipe recipe = catalog.recipes().get(line.recipeId());
        if (recipe == null) throw new StockRuleViolationException("Unknown recipe");
        if (!recipe.isActive()) throw new StockRuleViolationException("Recipe is inactive: " + recipe.getName());

        List<Requirement> result = new ArrayList<>();
        for (RecipeLine rl : recipe.getLines()) {                 // ingredient and units already loaded by the entity graph
            BigDecimal perUnit = conversion.toBase(rl.getIngredient(), rl.getUnit(), rl.getQuantity());
            BigDecimal total = perUnit.multiply(line.quantity()).setScale(4, RoundingMode.HALF_UP);
            if (total.signum() <= 0) {
                throw new StockRuleViolationException("Recipe quantity rounds to zero for "
                        + rl.getIngredient().getName());
            }
            requireActive(rl.getIngredient());
            result.add(new Requirement(rl.getIngredient(), total, line.sourceLineId()));
        }
        return result;
    }

    private static void requireActive(StockItem item) {
        if (!item.isActive()) throw new StockRuleViolationException("Stock item is inactive: " + item.getName());
    }

    @Override
    @Transactional
    public int reverseConsumption(ReverseConsumptionCommand cmd) {
        List<StockMovement> originals =
                movements.findReversible(cmd.sourceType(), cmd.sourceId(), cmd.sourceLineId());
        if (originals.isEmpty()) return 0;

        Map<BalanceKey, StockBalance> locked = ledger.lockAll(originals.stream()
                .map(m -> new BalanceKey(m.getStore().getId(), m.getItem().getId())).toList());

        for (StockMovement original : originals) {
            StockBalance balance = locked.get(new BalanceKey(original.getStore().getId(), original.getItem().getId()));
            ledger.reverse(balance, original, cmd.reason(), cmd.performedBy());
        }
        return originals.size();
    }
}
