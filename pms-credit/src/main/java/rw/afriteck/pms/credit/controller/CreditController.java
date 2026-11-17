package rw.afriteck.pms.credit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.credit.dtos.*;
import rw.afriteck.pms.credit.service.CreditCustomerService;
import rw.afriteck.pms.credit.service.CreditReportService;
import rw.afriteck.pms.credit.service.CreditService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor
public class CreditController {

    private final CreditCustomerService customerService;
    private final CreditService creditService;
    private final CreditReportService reportService;

    @PostMapping("/customers")
    // @PreAuthorize: CREDIT_CUSTOMER_MANAGE
    public ResponseEntity<CreditCustomerResponse> createCustomer(@Valid @RequestBody CreateCreditCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping("/customers/by-phone")
    public ResponseEntity<CreditCustomerResponse> findByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(customerService.findByPhone(phone));
    }

    @GetMapping("/customers/{customerId}/receivables")
    public ResponseEntity<List<ReceivableResponse>> listOutstanding(@PathVariable UUID customerId) {
        return ResponseEntity.ok(creditService.listOutstanding(customerId));
    }

    @PostMapping("/customers/{customerId}/repayments")
    // @PreAuthorize: DEBT_REPAY
    public ResponseEntity<RepayDebtResponse> repay(@PathVariable UUID customerId,
                                                   @Valid @RequestBody RepayDebtRequest request) {
        return ResponseEntity.ok(creditService.repay(customerId, request));
    }

    @PostMapping("/receivables/{receivableId}/write-off")
    // @PreAuthorize: DEBT_WRITE_OFF (manager-level)
    public ResponseEntity<WriteOffResponse> writeOff(@PathVariable UUID receivableId,
                                                     @Valid @RequestBody WriteOffRequest request) {
        return ResponseEntity.ok(creditService.writeOff(receivableId, request));
    }

    @GetMapping("/reports/aging")
    // @PreAuthorize: CREDIT_REPORT_VIEW
    public ResponseEntity<AgingReportResponse> agingReport(@RequestParam UUID hotelId) {
        return ResponseEntity.ok(reportService.agingReport(hotelId, LocalDate.now()));
    }
}
