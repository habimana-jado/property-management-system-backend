package rw.afriteck.pms.stock.api.setup;

import jakarta.validation.constraints.NotNull;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.UUID;

public record AssignmentCommand(@NotNull UUID storeId, @NotNull EOutletType outletType,
                                @NotNull UUID outletId, boolean makeDefault) {}
