package rw.afriteck.pms.stock.api;

import rw.afriteck.pms.stock.api.enums.EOutletType;
import rw.afriteck.pms.stock.api.setup.*;

import java.util.List;
import java.util.UUID;

public interface StructureSetupApi {
    List<StoreView> stores(boolean includeInactive);

    StoreView createStore(StoreCommand command);

    StoreView renameStore(UUID id, String name);

    void setStoreActive(UUID id, boolean active);

    List<AssignmentView> assignmentsForOutlet(EOutletType type, UUID outletId);

    List<AssignmentView> assignmentsForStore(UUID storeId);

    AssignmentView assign(AssignmentCommand command);

    void makeDefault(UUID assignmentId);

    void unassign(UUID assignmentId);

    BranchSettingsView branchSettings();

    BranchSettingsView updateBranchSettings(boolean allowNegativeStock);
}