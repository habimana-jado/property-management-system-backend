package rw.afriteck.pms.credit;

import rw.afriteck.pms.common.enums.EPayableType;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

final class CreditTestData {

    private CreditTestData() {}

    static CreditCustomer customer(UUID hotelId, String creditLimit) {
        CreditCustomer c = new CreditCustomer();
        c.setId(UUID.randomUUID()); // @GeneratedValue doesn't run in unit tests
        c.setHotelId(hotelId);
        c.setFullName("Test Customer");
        c.setPhoneNumber("+250788000001");
        c.setCreditAllowed(true);
        c.setActive(true);
        c.setCreditLimit(new BigDecimal(creditLimit));
        return c;
    }

    static Receivable receivable(CreditCustomer customer, String original, String paid,
                                 EReceivableStatus status, LocalDate dueDate, Instant issuedAt) {
        Receivable r = new Receivable();
        r.setId(UUID.randomUUID());
        r.setCustomer(customer);
        r.setSourceType(EPayableType.TABLE_BILL);
        r.setSourceId(UUID.randomUUID());
        r.setSourceReference("MNB-" + UUID.randomUUID().toString().substring(0, 6));
        r.setOriginalAmount(new BigDecimal(original));
        r.setAmountPaid(new BigDecimal(paid));
        r.setStatus(status);
        r.setDueDate(dueDate);
        r.setIssuedAt(issuedAt);
        return r;
    }
}
