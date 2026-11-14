package rw.afriteck.pms.stock.internal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.StockNotFoundException;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.property.model.HotelBranch;
import rw.afriteck.pms.stock.api.OutletDirectory;
import rw.afriteck.pms.stock.api.StructureSetupApi;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;
import rw.afriteck.pms.stock.api.enums.EStoreType;
import rw.afriteck.pms.stock.api.setup.*;
import rw.afriteck.pms.stock.internal.mapper.AssignmentMapper;
import rw.afriteck.pms.stock.internal.mapper.StoreMapper;
import rw.afriteck.pms.stock.internal.model.*;
import rw.afriteck.pms.stock.internal.repository.*;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
class StructureSetupServiceImpl implements StructureSetupApi {

    private static final List<EStocktakeStatus> OPEN_STOCKTAKE =
            List.of(EStocktakeStatus.DRAFT, EStocktakeStatus.COUNTING, EStocktakeStatus.SUBMITTED);

    private final StoreRepository stores;
    private final StoreAssignmentRepository assignments;
    private final StockBalanceRepository balances;
    private final StockTakeRepository stocktakes;
    private final BranchStockSettingRepository settings;
    private final DocumentSequenceRepository sequences;
    private final ObjectProvider<OutletDirectory> directories;     // zero beans is fine until an outlet module registers one
    private final BranchGuard guard;
    private final StoreMapper storeMapper;
    private final AssignmentMapper assignmentMapper;

    @PersistenceContext
    private EntityManager em;

    // ---------------- stores ----------------
    @Override @Transactional(readOnly = true)
    public List<StoreView> stores(boolean includeInactive) {
        return stores.findByBranchIdOrderByName(guard.currentBranchId()).stream()
                .filter(s -> includeInactive || s.isActive()).map(storeMapper::toView).toList();
    }

    @Override
    public StoreView createStore(StoreCommand c) {
        HotelBranch branch = em.find(HotelBranch.class, guard.currentBranchId());
        if (branch == null) throw new StockNotFoundException("Branch not found");

        String code = Codes.normalize(c.code());
        require(!stores.existsByBranchIdAndCode(branch.getId(), code), "Store code already exists in this branch");

        boolean branchHasMain = stores.existsByBranchIdAndStoreType(branch.getId(), EStoreType.MAIN);
        if (c.storeType() == EStoreType.MAIN) {
            require(!branchHasMain, "This branch already has a main store");
        } else {
            require(branchHasMain, "Create the branch's main store first");
        }

        Store s = new Store();
        s.setBranch(branch);
        s.setCode(code);
        s.setName(c.name().trim());
        s.setStoreType(c.storeType());
        stores.save(s);                                            // @PrePersist sets mainStoreKey

        if (c.storeType() == EStoreType.MAIN) seedBranchDefaults(branch);
        return storeMapper.toView(s);
    }

    /** What document numbering and the negative-stock setting need before the branch can post anything. */
    private void seedBranchDefaults(HotelBranch branch) {
        for (EDocumentType type : EDocumentType.values()) {
            if (!sequences.existsByBranchIdAndDocumentType(branch.getId(), type)) {
                DocumentSequence seq = new DocumentSequence();
                seq.setBranch(branch);
                seq.setDocumentType(type);
                sequences.save(seq);
            }
        }
        if (settings.findByBranchId(branch.getId()).isEmpty()) {
            BranchStockSetting s = new BranchStockSetting();
            s.setBranch(branch);
            settings.save(s);
        }
    }

    @Override
    public StoreView renameStore(UUID id, String name) {
        Store s = store(id);
        s.setName(name.trim());
        return storeMapper.toView(s);
    }

    @Override
    public void setStoreActive(UUID id, boolean active) {
        Store s = store(id);
        if (!active) {
            require(s.getStoreType() != EStoreType.MAIN, "The main store cannot be deactivated");
            require(!balances.existsByStoreIdAndQuantityOnHandNot(id, BigDecimal.ZERO),
                    "Transfer or write off the remaining stock first");
            require(!assignments.existsByStoreIdAndActiveTrue(id), "Unassign the outlets that use this store first");
            require(!stocktakes.existsByStoreIdAndStatusIn(id, OPEN_STOCKTAKE),
                    "Finish or cancel the open stocktake first");
        }
        s.setActive(active);
    }

    // ---------------- assignments ----------------
    @Override @Transactional(readOnly = true)
    public List<AssignmentView> assignmentsForOutlet(EOutletType type, UUID outletId) {
        return assignments.findByOutletTypeAndOutletId(type, outletId).stream()
                .filter(a -> guard.inCurrentBranch(a.getStore())).map(assignmentMapper::toView).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<AssignmentView> assignmentsForStore(UUID storeId) {
        return assignments.findByStoreId(storeId).stream()
                .filter(a -> guard.inCurrentBranch(a.getStore())).map(assignmentMapper::toView).toList();
    }

    @Override
    public AssignmentView assign(AssignmentCommand c) {
        Store s = store(c.storeId());
        require(s.isActive(), "Store is inactive: " + s.getName());
        require(s.getStoreType() != EStoreType.DAMAGED, "The damaged store cannot serve an outlet");

        UUID outletBranchId = directory(c.outletType()).branchIdOf(c.outletId())
                .orElseThrow(() -> new StockNotFoundException("Outlet not found"));
        require(outletBranchId.equals(s.getBranch().getId()), "The outlet and the store belong to different branches");

        StoreAssignment a = assignments.findByStoreIdAndOutletTypeAndOutletId(s.getId(), c.outletType(), c.outletId())
                .orElseGet(() -> {
                    StoreAssignment n = new StoreAssignment();
                    n.setStore(s);
                    n.setOutletType(c.outletType());
                    n.setOutletId(c.outletId());
                    return n;
                });

        List<StoreAssignment> others = assignments
                .findByOutletTypeAndOutletIdAndActiveTrue(c.outletType(), c.outletId()).stream()
                .filter(x -> !x.getStore().getId().equals(s.getId())).toList();

        // the outlet's first store becomes its default automatically
        boolean becomesDefault = c.makeDefault() || others.stream().noneMatch(StoreAssignment::isDefault);
        if (becomesDefault) clearDefaults(others);

        a.setActive(true);
        a.setDefault(becomesDefault);
        return assignmentMapper.toView(assignments.save(a));
    }

    @Override
    public void makeDefault(UUID assignmentId) {
        StoreAssignment a = assignment(assignmentId);
        require(a.isActive(), "The assignment is inactive");
        clearDefaults(assignments.findByOutletTypeAndOutletIdAndActiveTrue(a.getOutletType(), a.getOutletId()).stream()
                .filter(x -> !x.getId().equals(a.getId())).toList());
        a.setDefault(true);
    }

    @Override
    public void unassign(UUID assignmentId) {
        StoreAssignment a = assignment(assignmentId);
        boolean otherActive = assignments.findByOutletTypeAndOutletIdAndActiveTrue(a.getOutletType(), a.getOutletId())
                .stream().anyMatch(x -> !x.getId().equals(a.getId()));
        require(!(a.isDefault() && otherActive), "Choose another default store for this outlet first");
        a.setActive(false);
        a.setDefault(false);
    }

    /** The unique key on the default flag means the old default must be cleared and written BEFORE the new one is set. */
    private void clearDefaults(List<StoreAssignment> others) {
        boolean changed = false;
        for (StoreAssignment x : others) {
            if (x.isDefault()) { x.setDefault(false); changed = true; }
        }
        if (changed) assignments.flush();
    }

    // ---------------- branch settings ----------------
    @Override @Transactional(readOnly = true)
    public BranchSettingsView branchSettings() {
        UUID branchId = guard.currentBranchId();
        return settings.findByBranchId(branchId)
                .map(s -> new BranchSettingsView(branchId, s.isAllowNegativeStock()))
                .orElse(new BranchSettingsView(branchId, false));
    }

    @Override
    public BranchSettingsView updateBranchSettings(boolean allowNegativeStock) {
        UUID branchId = guard.currentBranchId();
        BranchStockSetting s = settings.findByBranchId(branchId).orElseGet(() -> {
            HotelBranch branch = em.find(HotelBranch.class, branchId);
            if (branch == null) throw new StockNotFoundException("Branch not found");
            BranchStockSetting n = new BranchStockSetting();
            n.setBranch(branch);
            return n;
        });
        s.setAllowNegativeStock(allowNegativeStock);
        settings.save(s);
        return new BranchSettingsView(branchId, allowNegativeStock);
    }

    // ---------------- helpers ----------------
    private OutletDirectory directory(EOutletType type) {
        return directories.orderedStream().filter(d -> d.type() == type).findFirst()
                .orElseThrow(() -> new StockRuleViolationException(
                        "Outlets of type " + type + " cannot be assigned yet"));
    }
    private Store store(UUID id) {
        return stores.findById(id).filter(guard::inCurrentBranch)
                .orElseThrow(() -> new StockNotFoundException("Store not found"));
    }
    private StoreAssignment assignment(UUID id) {
        return assignments.findById(id).filter(a -> guard.inCurrentBranch(a.getStore()))
                .orElseThrow(() -> new StockNotFoundException("Assignment not found"));
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new StockRuleViolationException(message);
    }
}