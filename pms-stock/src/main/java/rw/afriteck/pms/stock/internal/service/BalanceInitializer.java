package rw.afriteck.pms.stock.internal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.stock.internal.model.StockBalance;
import rw.afriteck.pms.stock.internal.model.StockItem;
import rw.afriteck.pms.stock.internal.model.Store;
import rw.afriteck.pms.stock.internal.repository.StockBalanceRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class BalanceInitializer {

    private final StockBalanceRepository balances;
    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createIfAbsent(UUID storeId, UUID itemId) {
        if (balances.existsByStoreIdAndItemId(storeId, itemId)) return;
        StockBalance b = new StockBalance();
        b.setStore(em.getReference(Store.class, storeId));
        b.setItem(em.getReference(StockItem.class, itemId));
        balances.saveAndFlush(b);              // unique violation propagates to the caller
    }
}
