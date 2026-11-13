package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record RecipeLineCommand(@NotNull UUID ingredientId, @NotNull @Positive BigDecimal quantity, @NotNull UUID unitId) {}

