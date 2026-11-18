package rw.afriteck.pms.credit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.afriteck.pms.credit.dtos.AgingBucketTotals;
import rw.afriteck.pms.credit.dtos.AgingReportResponse;
import rw.afriteck.pms.credit.dtos.CustomerAgingRow;
import rw.afriteck.pms.credit.entity.CreditCustomer;
import rw.afriteck.pms.credit.entity.Receivable;
import rw.afriteck.pms.credit.enums.EReceivableStatus;
import rw.afriteck.pms.credit.repository.ReceivableRepository;
import rw.afriteck.pms.credit.service.CreditReportService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditReportServiceTest {

    @Mock
    ReceivableRepository receivableRepository;
    @InjectMocks
    CreditReportService reportService;

    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 17);
    private final UUID hotelId = UUID.randomUUID();

    @Test
    void agingReport_groupsByCustomerAndBucket_sortsLargestDebtorFirst_andTotalsCorrectly() {
        CreditCustomer a = CreditTestData.customer(hotelId, "100000");
        CreditCustomer b = CreditTestData.customer(hotelId, "100000");
        b.setFullName("Big Debtor");

        // A: 5,000 not yet due + 15,000 outstanding (20,000 - 5,000 paid), 45 days overdue
        Receivable a1 = CreditTestData.receivable(a, "5000", "0",
                EReceivableStatus.OUTSTANDING, AS_OF.plusDays(5), Instant.now());
        Receivable a2 = CreditTestData.receivable(a, "20000", "5000",
                EReceivableStatus.PARTIALLY_SETTLED, AS_OF.minusDays(45), Instant.now());
        // B: 30,000, 100 days overdue
        Receivable b1 = CreditTestData.receivable(b, "30000", "0",
                EReceivableStatus.OUTSTANDING, AS_OF.minusDays(100), Instant.now());

        when(receivableRepository.findActiveByHotelBranchWithCustomer(eq(hotelId), any()))
                .thenReturn(List.of(a1, a2, b1));

        AgingReportResponse report = reportService.agingReport(hotelId, AS_OF);

        assertThat(report.asOf()).isEqualTo(AS_OF);
        assertThat(report.customers()).hasSize(2);

        // largest debtor first (B owes 30,000, A owes 20,000)
        CustomerAgingRow first = report.customers().get(0);
        CustomerAgingRow second = report.customers().get(1);
        assertThat(first.customerId()).isEqualTo(b.getId());
        assertThat(first.totals().over90()).isEqualByComparingTo("30000");
        assertThat(first.totals().total()).isEqualByComparingTo("30000");

        assertThat(second.customerId()).isEqualTo(a.getId());
        assertThat(second.totals().current()).isEqualByComparingTo("5000");
        assertThat(second.totals().days31To60()).isEqualByComparingTo("15000"); // outstanding, not original
        assertThat(second.totals().total()).isEqualByComparingTo("20000");

        AgingBucketTotals grand = report.grandTotals();
        assertThat(grand.current()).isEqualByComparingTo("5000");
        assertThat(grand.days31To60()).isEqualByComparingTo("15000");
        assertThat(grand.over90()).isEqualByComparingTo("30000");
        assertThat(grand.total()).isEqualByComparingTo("50000");
    }

    @Test
    void agingReport_onlyRequestsActiveDebts() {
        when(receivableRepository.findActiveByHotelBranchWithCustomer(eq(hotelId), any())).thenReturn(List.of());

        reportService.agingReport(hotelId, AS_OF);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EReceivableStatus>> captor = ArgumentCaptor.forClass(List.class);
        verify(receivableRepository).findActiveByHotelBranchWithCustomer(eq(hotelId), captor.capture());

        // guards against someone adding SETTLED or WRITTEN_OFF to the report by accident
        assertThat(captor.getValue())
                .containsExactlyInAnyOrder(EReceivableStatus.OUTSTANDING, EReceivableStatus.PARTIALLY_SETTLED);
    }

    @Test
    void agingReport_returnsZeroedTotalsWhenNothingIsOwed() {
        when(receivableRepository.findActiveByHotelBranchWithCustomer(eq(hotelId), any())).thenReturn(List.of());

        AgingReportResponse report = reportService.agingReport(hotelId, AS_OF);

        assertThat(report.customers()).isEmpty();
        assertThat(report.grandTotals().total()).isEqualByComparingTo("0");
    }
}
