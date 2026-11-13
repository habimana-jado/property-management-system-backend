package rw.afriteck.pms.stock.api.setup;

import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.UUID;

public record AssignmentView(UUID id, UUID storeId, String storeName, EOutletType outletType, UUID outletId,
                             boolean defaultStore, boolean active) {}
