package rw.afriteck.pms.credit.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record CreditCustomerResponse(
        UUID id,
        String fullName,
        String phoneNumber,
        boolean creditAllowed,
        BigDecimal creditLimit,
        BigDecimal outstandingBalance,
        BigDecimal availableCredit
) {}
