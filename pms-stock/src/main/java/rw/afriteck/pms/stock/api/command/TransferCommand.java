package rw.afriteck.pms.stock.api.command;

import rw.afriteck.pms.stock.api.enums.ETransferPurpose;

import java.util.List;
import java.util.UUID;

public record TransferCommand(
        UUID fromStoreId,
        UUID toStoreId,
        ETransferPurpose purpose,
        String note,
        List<DocumentLineInput> lines,
        UUID performedBy
) {
}
