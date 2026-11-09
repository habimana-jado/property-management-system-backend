package rw.afriteck.pms.stock.api.view;

import java.math.BigDecimal;
import java.util.UUID;

public record StoreQuantity(UUID storeId, String storeName, BigDecimal quantityOnHand) {}

