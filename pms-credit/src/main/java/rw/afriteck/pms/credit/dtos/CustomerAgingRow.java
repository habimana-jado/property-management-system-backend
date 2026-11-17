package rw.afriteck.pms.credit.dtos;

import java.util.UUID;

public record CustomerAgingRow(
        UUID customerId,
        String customerName,
        String phoneNumber,
        AgingBucketTotals totals
) {}
