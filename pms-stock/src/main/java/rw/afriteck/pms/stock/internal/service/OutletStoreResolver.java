package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.internal.enums.EStoreType;
import rw.afriteck.pms.stock.internal.model.Store;
import rw.afriteck.pms.stock.internal.model.StoreAssignment;
import rw.afriteck.pms.stock.internal.repository.StoreAssignmentRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class OutletStoreResolver {

    private final StoreAssignmentRepository assignments;

    Store resolveIssuingStore(EOutletType type, UUID outletId, UUID requestedStoreId) {
        List<StoreAssignment> active = assignments.findByOutletTypeAndOutletIdAndActiveTrue(type, outletId);
        StoreAssignment chosen = (requestedStoreId == null)
                ? active.stream().filter(StoreAssignment::isDefault).findFirst()
                .orElseThrow(() -> new StockRuleViolationException("Outlet has no default store"))
                : active.stream().filter(a -> a.getStore().getId().equals(requestedStoreId)).findFirst()
                .orElseThrow(() -> new StockRuleViolationException("Store is not assigned to this outlet"));
        Store store = chosen.getStore();
        if (!store.isActive() || store.getStoreType() == EStoreType.DAMAGED) {
            throw new StockRuleViolationException("Store cannot issue stock: " + store.getName());
        }
        return store;
    }
}
