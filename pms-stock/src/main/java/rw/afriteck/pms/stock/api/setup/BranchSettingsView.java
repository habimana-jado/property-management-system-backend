package rw.afriteck.pms.stock.api.setup;

import java.util.UUID;

public record BranchSettingsView(UUID branchId, boolean allowNegativeStock) {}

