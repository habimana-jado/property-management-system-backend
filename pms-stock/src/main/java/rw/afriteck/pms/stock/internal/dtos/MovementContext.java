package rw.afriteck.pms.stock.internal.dtos;

import rw.afriteck.pms.stock.api.enums.EMovementSourceType;
import rw.afriteck.pms.stock.internal.model.StockDocumentLine;
import rw.afriteck.pms.stock.internal.model.StockMovement;

import java.util.UUID;

public record MovementContext(
        StockDocumentLine documentLine,
        EMovementSourceType sourceType,
        UUID sourceId,
        UUID sourceLineId,
        StockMovement reversalOf,
        String reason,
        String note,
        UUID performedBy
) {

    public static MovementContext forSale(EMovementSourceType type, UUID sourceId, UUID lineId, UUID by) {
        return new MovementContext(null, type, sourceId, lineId, null, null, null, by);
    }
    public static MovementContext forDocument(StockDocumentLine line, String note, UUID by) {
        return new MovementContext(line, null, null, null, null, null, note, by);
    }
    public static MovementContext forReversal(StockMovement original, String reason, UUID by) {
        return new MovementContext(original.getDocumentLine(), original.getSourceType(),
                original.getSourceId(), original.getSourceLineId(), original, reason, null, by);
    }
}
