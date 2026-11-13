package rw.afriteck.pms.stock.api.setup;

import java.math.BigDecimal;
import java.util.UUID;

public record RecipeLineView(UUID ingredientId, String ingredientCode, String ingredientName,
                             BigDecimal quantity, UUID unitId, String unitCode) {}
