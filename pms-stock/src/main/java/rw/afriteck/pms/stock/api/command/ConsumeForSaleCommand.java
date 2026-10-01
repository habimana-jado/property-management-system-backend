package rw.afriteck.pms.stock.api.command;

import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.api.enums.EOutletType;

import java.util.List;
import java.util.UUID;

public record ConsumeForSaleCommand(
        EOutletType outletType,
        UUID outletId,
        UUID storeId,
        EMovementSourceType sourceType,
        UUID sourceId,
        List<SaleLine> lines,
        UUID performedBy
) {
}
