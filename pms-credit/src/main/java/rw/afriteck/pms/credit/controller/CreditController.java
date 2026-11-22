package rw.afriteck.pms.credit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.credit.dtos.*;
import rw.afriteck.pms.credit.security.CreditPermissions;
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
    @PreAuthorize(CreditPermissions.Has.CUSTOMER_MANAGE)
    public ResponseEntity<CreditCustomerResponse> createCustomer(@Valid @RequestBody CreateCreditCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping("/customers/by-phone")
    @PreAuthorize(CreditPermissions.Has.CUSTOMER_VIEW)
    public ResponseEntity<CreditCustomerResponse> findByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(customerService.findByPhone(phone));
    }

    @GetMapping("/customers/{customerId}/receivables")
    @PreAuthorize(CreditPermissions.Has.RECEIVABLE_VIEW)
    public ResponseEntity<List<ReceivableResponse>> listOutstanding(@PathVariable UUID customerId) {
        return ResponseEntity.ok(creditService.listOutstanding(customerId));
    }

    @PostMapping("/customers/{customerId}/repayments")
    @PreAuthorize(CreditPermissions.Has.REPAY)
    public ResponseEntity<RepayDebtResponse> repay(@PathVariable UUID customerId,
                                                   @Valid @RequestBody RepayDebtRequest request) {
        return ResponseEntity.ok(creditService.repay(customerId, request));
    }

    @PostMapping("/receivables/{receivableId}/write-off")
    @PreAuthorize(CreditPermissions.Has.WRITE_OFF)
    public ResponseEntity<WriteOffResponse> writeOff(@PathVariable UUID receivableId,
                                                     @Valid @RequestBody WriteOffRequest request) {
        return ResponseEntity.ok(creditService.writeOff(receivableId, request));
    }

    @GetMapping("/reports/aging")
    @PreAuthorize(CreditPermissions.Has.REPORT_VIEW)
    public ResponseEntity<AgingReportResponse> agingReport(@RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(reportService.agingReport(branchId, LocalDate.now()));
    }
}
