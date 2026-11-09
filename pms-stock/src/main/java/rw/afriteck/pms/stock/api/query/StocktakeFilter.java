package rw.afriteck.pms.stock.api.query;

import rw.afriteck.pms.stock.api.enums.EStocktakeStatus;

import java.util.UUID;

public record StocktakeFilter(UUID branchId, UUID storeId, EStocktakeStatus status) {}

