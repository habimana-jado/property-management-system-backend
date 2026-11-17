package rw.afriteck.pms.credit.dtos;

import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.credit.enums.EReceivableStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceivableResponse(
        UUID id,
        UUID customerId,
        String customerName,
        EPayableType sourceType,
        String sourceReference,
        BigDecimal originalAmount,
        BigDecimal amountPaid,
        BigDecimal outstandingBalance,
        LocalDate dueDate,
        EReceivableStatus status,
        boolean overdue
) {}
