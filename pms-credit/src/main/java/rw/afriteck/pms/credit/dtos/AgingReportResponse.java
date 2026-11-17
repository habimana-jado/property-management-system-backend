package rw.afriteck.pms.credit.dtos;

import java.time.LocalDate;
import java.util.List;

public record AgingReportResponse(
        LocalDate asOf,
        List<CustomerAgingRow> customers,   // largest debtor first
        AgingBucketTotals grandTotals
) {}
