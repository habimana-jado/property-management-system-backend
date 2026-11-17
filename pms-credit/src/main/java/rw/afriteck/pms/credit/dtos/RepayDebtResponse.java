package rw.afriteck.pms.credit.dtos;

import java.math.BigDecimal;
import java.util.List;

public record RepayDebtResponse(
        BigDecimal totalApplied,
        BigDecimal customerOutstandingBalance,
        List<ReceivableAllocationResponse> allocations
) {}
