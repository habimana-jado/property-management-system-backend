package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PackagingCommand(@NotNull UUID unitId, @NotNull @Positive BigDecimal quantityInBaseUnit) {}

