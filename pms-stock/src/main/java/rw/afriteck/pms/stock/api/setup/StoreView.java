package rw.afriteck.pms.stock.api.setup;

import rw.afriteck.pms.stock.api.enums.EStoreType;

import java.util.UUID;

public record StoreView(UUID id, UUID branchId, String code, String name, EStoreType storeType, boolean active) {}

