package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.stock.api.StockAvailabilityApi;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.view.Availability;
import rw.afriteck.pms.stock.internal.model.Recipe;
import rw.afriteck.pms.stock.internal.model.RecipeLine;
import rw.afriteck.pms.stock.internal.model.StockBalance;
import rw.afriteck.pms.stock.internal.model.Store;
import rw.afriteck.pms.stock.internal.repository.RecipeRepository;
import rw.afriteck.pms.stock.internal.repository.StockBalanceRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class StockAvailabilityServiceImpl implements StockAvailabilityApi {

    private final OutletStoreResolver storeResolver;
    private final StockBalanceRepository balances;
    private final RecipeRepository recipes;
    private final UnitOfConversionService conversion;

    @Override
    public Availability availability(EOutletType outletType, UUID outletId, UUID storeId,
                                     Collection<UUID> stockItemIds, Collection<UUID> recipeIds) {
        Store store = storeResolver.resolveIssuingStore(outletType, outletId, storeId);

        List<Recipe> recipeList = recipeIds.isEmpty() ? List.of() : recipes.findWithLinesByIdIn(recipeIds);

        Set<UUID> needed = new HashSet<>(stockItemIds);
        recipeList.forEach(r -> r.getLines().forEach(l -> needed.add(l.getIngredient().getId())));

        Map<UUID, BigDecimal> onHand = needed.isEmpty() ? Map.of()
                : balances.findByStoreIdAndItemIdIn(store.getId(), needed).stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), StockBalance::getQuantityOnHand));

        Map<UUID, BigDecimal> items = new HashMap<>();
        stockItemIds.forEach(id -> items.put(id, onHand.getOrDefault(id, BigDecimal.ZERO)));

        Map<UUID, BigDecimal> portions = new HashMap<>();
        recipeList.forEach(r -> portions.put(r.getId(), portionsPossible(r, onHand)));
        return new Availability(items, portions);
    }

    /** The scarcest ingredient decides: min over lines of floor(on hand / needed per portion). */
    private BigDecimal portionsPossible(Recipe recipe, Map<UUID, BigDecimal> onHand) {
        BigDecimal min = null;
        for (RecipeLine line : recipe.getLines()) {
            BigDecimal perPortion = conversion.toBase(line.getIngredient(), line.getUnit(), line.getQuantity());
            if (perPortion.signum() <= 0) continue;
            BigDecimal have = onHand.getOrDefault(line.getIngredient().getId(), BigDecimal.ZERO).max(BigDecimal.ZERO);
            BigDecimal possible = have.divide(perPortion, 0, RoundingMode.DOWN);
            min = (min == null) ? possible : min.min(possible);
        }
        return min == null ? BigDecimal.ZERO : min;
    }
}
