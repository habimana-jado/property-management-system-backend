package rw.afriteck.pms.stock.api.setup;

import java.math.BigDecimal;
import java.util.UUID;

public record PackagingView(UUID unitId, String unitCode, BigDecimal quantityInBaseUnit) {}

