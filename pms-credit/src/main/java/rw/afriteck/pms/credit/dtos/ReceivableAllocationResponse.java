package rw.afriteck.pms.credit.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ReceivableAllocationResponse(
        UUID receivableId,
        String sourceReference,
        BigDecimal applied,
        BigDecimal remaining,
        boolean settled
) {}
