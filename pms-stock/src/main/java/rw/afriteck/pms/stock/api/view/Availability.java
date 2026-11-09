package rw.afriteck.pms.stock.api.view;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/** Base-unit quantities on hand, and the number of portions each recipe could still be made. */
public record Availability(Map<UUID, BigDecimal> itemQuantities, Map<UUID, BigDecimal> recipePortions) {}

