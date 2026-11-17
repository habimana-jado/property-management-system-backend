package rw.afriteck.pms.credit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.credit.dtos.AgingBucketTotals;
import rw.afriteck.pms.credit.dtos.AgingReportResponse;
import rw.afriteck.pms.credit.dtos.CustomerAgingRow;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EAgingBucket;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.ReceivableRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CreditReportService {

    private static final List<EReceivableStatus> ACTIVE =
            List.of(EReceivableStatus.OUTSTANDING, EReceivableStatus.PARTIALLY_SETTLED);

    private final ReceivableRepository receivableRepository;

    @Transactional(readOnly = true)
    public AgingReportResponse agingReport(UUID hotelId, LocalDate asOf) {

        List<Receivable> active = receivableRepository.findActiveByHotelWithCustomer(hotelId, ACTIVE);

        Map<UUID, CreditCustomer> customers = new HashMap<>();
        Map<UUID, Map<EAgingBucket, BigDecimal>> perCustomer = new HashMap<>();
        Map<EAgingBucket, BigDecimal> grand = emptyBuckets();

        for (Receivable r : active) {
            EAgingBucket bucket = EAgingBucket.of(r.getDueDate(), asOf);
            BigDecimal outstanding = r.outstandingBalance();
            CreditCustomer c = r.getCustomer();

            customers.putIfAbsent(c.getId(), c);
            perCustomer.computeIfAbsent(c.getId(), id -> emptyBuckets())
                    .merge(bucket, outstanding, BigDecimal::add);
            grand.merge(bucket, outstanding, BigDecimal::add);
        }

        List<CustomerAgingRow> rows = perCustomer.entrySet().stream()
                .map(e -> {
                    CreditCustomer c = customers.get(e.getKey());
                    return new CustomerAgingRow(c.getId(), c.getFullName(), c.getPhoneNumber(),
                            AgingBucketTotals.from(e.getValue()));
                })
                .sorted(Comparator.comparing((CustomerAgingRow row) -> row.totals().total()).reversed())
                .toList();

        return new AgingReportResponse(asOf, rows, AgingBucketTotals.from(grand));
    }

    private Map<EAgingBucket, BigDecimal> emptyBuckets() {
        Map<EAgingBucket, BigDecimal> m = new EnumMap<>(EAgingBucket.class);
        for (EAgingBucket b : EAgingBucket.values()) {
            m.put(b, BigDecimal.ZERO);
        }
        return m;
    }
}
