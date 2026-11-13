package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import rw.afriteck.pms.stock.api.enums.EItemType;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemUpdateCommand(@NotBlank @Size(max = 200) String name, @Size(max = 1000) String description,
                                @NotNull UUID categoryId, @NotNull UUID baseUnitId,
                                @NotNull EItemType itemType, @PositiveOrZero BigDecimal reorderLevel) {}
