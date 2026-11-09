package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.InsufficientStockException;
import rw.afriteck.pms.stock.internal.dtos.BalanceKey;
import rw.afriteck.pms.stock.internal.dtos.MovementContext;
import rw.afriteck.pms.stock.api.enums.EMovementType;
import rw.afriteck.pms.stock.internal.model.BranchStockSetting;
import rw.afriteck.pms.stock.internal.model.StockBalance;
import rw.afriteck.pms.stock.internal.model.StockMovement;
import rw.afriteck.pms.stock.internal.model.Store;
import rw.afriteck.pms.stock.internal.repository.BranchStockSettingRepository;
import rw.afriteck.pms.stock.internal.repository.StockBalanceRepository;
import rw.afriteck.pms.stock.internal.repository.StockMovementRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
class StockLedger {

    private final StockBalanceRepository balances;
    private final StockMovementRepository movements;
    private final BalanceInitializer initializer;
    private final BranchStockSettingRepository settings;

    /** Locks every (store,item) balance in a deterministic order so concurrent
     *  transactions can never deadlock on each other. Call BEFORE reading balances. */
    Map<BalanceKey, StockBalance> lockAll(Collection<BalanceKey> keys) {
        // stores in id order, items within a store in id order: the same global lock order as before
        Map<UUID, Set<UUID>> itemsByStore = new TreeMap<>();
        for (BalanceKey k : keys) {
            itemsByStore.computeIfAbsent(k.storeId(), s -> new TreeSet<>()).add(k.itemId());
        }

        Map<BalanceKey, StockBalance> locked = new LinkedHashMap<>();
        itemsByStore.forEach((storeId, itemIds) -> {
            List<StockBalance> found = balances.findAllForUpdate(storeId, itemIds);

            Set<UUID> missing = new TreeSet<>(itemIds);
            found.forEach(b -> missing.remove(b.getItem().getId()));
            if (!missing.isEmpty()) {                          // rare: first ever movement of an item in a store
                missing.forEach(itemId -> createQuietly(storeId, itemId));
                found = balances.findAllForUpdate(storeId, itemIds);   // re-lock, now including the new rows
                if (found.size() != itemIds.size()) {                       // NEW: fail loudly, not with a later NPE
                    throw new IllegalStateException("Balance rows missing after creation for store " + storeId);
                }
            }
            found.forEach(b -> locked.put(new BalanceKey(storeId, b.getItem().getId()), b));
        });
        return locked;
    }

    private void createQuietly(UUID storeId, UUID itemId) {
        try {
            initializer.createIfAbsent(storeId, itemId);
        } catch (DataIntegrityViolationException createdConcurrently) {
            // another transaction created it first: fine
        }
    }

    StockMovement post(StockBalance balance, EMovementType type, BigDecimal delta, MovementContext ctx) {
        Store store = balance.getStore();
        BigDecimal after = balance.getQuantityOnHand().add(delta);
        if (after.signum() < 0 && !allowsNegative(type, store)) {
            throw new InsufficientStockException(store.getName(), balance.getItem().getName(),
                    balance.getQuantityOnHand(), delta.negate());
        }
        balance.apply(delta);

        StockMovement m = new StockMovement();
        m.setStore(store);
        m.setItem(balance.getItem());
        m.setMovementType(type);
        m.setQuantity(delta);
        m.setBalanceAfter(balance.getQuantityOnHand());
        m.setDocumentLine(ctx.documentLine());
        m.setSourceType(ctx.sourceType());
        m.setSourceId(ctx.sourceId());
        m.setSourceLineId(ctx.sourceLineId());
        m.setReversalOf(ctx.reversalOf());
        m.setReason(ctx.reason());
        m.setNote(ctx.note());
        m.setPerformedBy(ctx.performedBy());
        m.setOccurredAt(Instant.now());
        return movements.save(m);
    }

    StockMovement reverse(StockBalance balance, StockMovement original, String reason, UUID by) {
        return post(balance, original.getMovementType(), original.getQuantity().negate(),
                MovementContext.forReversal(original, reason, by));
    }

    /** The branch setting applies to SALE and CONSUMPTION only. Adjustments (stocktake
     *  corrections) must always be recordable; every other movement never goes negative. */
    private boolean allowsNegative(EMovementType type, Store store) {
        return switch (type) {
            case ADJUSTMENT -> true;
            case SALE, CONSUMPTION -> settings.findByBranchId(store.getBranch().getId())
                    .map(BranchStockSetting::isAllowNegativeStock).orElse(false);
            default -> false;
        };
    }
}
