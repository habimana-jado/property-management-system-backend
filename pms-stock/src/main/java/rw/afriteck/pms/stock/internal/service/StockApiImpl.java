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
import rw.afriteck.pms.stock.internal.enums.EMovementType;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.RecipeRepository;
import rw.afriteck.pms.stock.internal.repository.StockItemRepository;
import rw.afriteck.pms.stock.internal.repository.StockMovementRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Override
    @Transactional                                  // joins the caller's transaction
    public List<UUID> consumeForSale(ConsumeForSaleCommand cmd) {
        Store store = storeResolver.resolveIssuingStore(cmd.outletType(), cmd.outletId(), cmd.storeId());

        // 1. expand sale lines (direct items and recipes) into per-ingredient requirements
        List<Requirement> requirements = new ArrayList<>();
        for (SaleLine line : cmd.lines()) {
            if (line.trackInventory()) requirements.addAll(expand(line));
        }
        if (requirements.isEmpty()) return List.of();

        // 2. lock all affected balances in deterministic order
        Map<BalanceKey, StockBalance> locked = ledger.lockAll(requirements.stream()
                .map(r -> new BalanceKey(store.getId(), r.item().getId())).toList());

        // 3. one SALE movement per requirement; any failure rolls back the whole sale
        List<UUID> ids = new ArrayList<>();
        for (Requirement r : requirements) {
            StockBalance balance = locked.get(new BalanceKey(store.getId(), r.item().getId()));
            ids.add(ledger.post(balance, EMovementType.SALE, r.quantity().negate(),
                    MovementContext.forSale(cmd.sourceType(), cmd.sourceId(), r.sourceLineId(),
                            cmd.performedBy())).getId());
        }
        return ids;
    }

    private List<Requirement> expand(SaleLine line) {
        if ((line.stockItemId() == null) == (line.recipeId() == null)) {
            throw new StockRuleViolationException("A sale line needs exactly one of stockItemId or recipeId");
        }
        if (line.quantity() == null || line.quantity().signum() <= 0) {
            throw new StockRuleViolationException("Sale quantity must be positive");
        }
        if (line.stockItemId() != null) {
            StockItem item = items.findById(line.stockItemId()).orElseThrow(
                    () -> new StockRuleViolationException("Unknown stock item"));
            requireActive(item);
            return List.of(new Requirement(item, line.quantity(), line.sourceLineId()));
        }
        Recipe recipe = recipes.findWithLinesById(line.recipeId()).orElseThrow(
                () -> new StockRuleViolationException("Unknown recipe"));
        if (!recipe.isActive()) throw new StockRuleViolationException("Recipe is inactive: " + recipe.getName());
        List<Requirement> result = new ArrayList<>();
        for (RecipeLine rl : recipe.getLines()) {
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
