package rw.afriteck.pms.credit.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WriteOffResponse(
        UUID receivableId,
        String customerName,
        String sourceReference,
        BigDecimal writtenOffAmount,
        String reason,
        String approvedBy,
        Instant writtenOffAt
) {}
