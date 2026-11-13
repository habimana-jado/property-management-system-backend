package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.afriteck.pms.common.security.CurrentUserService; // adjust to the real package in pms-common
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.web.dtos.*;
import rw.afriteck.pms.stock.web.security.StockPermissions;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock")
@RequiredArgsConstructor
public class StockDocumentController {

    private final StockOperations operations;
    private final CurrentUserService currentUser;

    @PostMapping("/receipts")
    @PreAuthorize(StockPermissions.Has.RECEIPT_POST)
    public ResponseEntity<DocumentCreatedResponse> receipt(@Valid @RequestBody ReceiptRequest r) {
        return post(EDocumentType.RECEIPT, r.storeId(), r.supplierName(), r.externalReference(), r.note(), r.lines());
    }

    @PostMapping("/opening-balances")
    @PreAuthorize(StockPermissions.Has.ADJUSTMENT_POST)
    public ResponseEntity<DocumentCreatedResponse> openingBalance(@Valid @RequestBody StockDocumentRequest r) {
        return post(EDocumentType.OPENING_BALANCE, r.storeId(), null, r.externalReference(), r.note(), r.lines());
    }

    @PostMapping("/adjustments")
    @PreAuthorize(StockPermissions.Has.ADJUSTMENT_POST)
    public ResponseEntity<DocumentCreatedResponse> adjustment(@Valid @RequestBody AdjustmentRequest r) {
        return post(EDocumentType.ADJUSTMENT, r.storeId(), null, r.externalReference(), r.reason(), r.lines());
    }

    @PostMapping("/write-offs")
    @PreAuthorize(StockPermissions.Has.WRITE_OFF_POST)
    public ResponseEntity<DocumentCreatedResponse> writeOff(@Valid @RequestBody ReasonedDocumentRequest r) {
        return post(EDocumentType.WRITE_OFF, r.storeId(), null, r.externalReference(), r.reason(), r.lines());
    }

    @PostMapping("/supplier-returns")
    @PreAuthorize(StockPermissions.Has.RECEIPT_POST)
    public ResponseEntity<DocumentCreatedResponse> supplierReturn(@Valid @RequestBody ReasonedDocumentRequest r) {
        return post(EDocumentType.SUPPLIER_RETURN, r.storeId(), null, r.externalReference(), r.reason(), r.lines());
    }

    @PostMapping("/customer-returns")
    @PreAuthorize(StockPermissions.Has.CONSUMPTION_POST)
    public ResponseEntity<DocumentCreatedResponse> customerReturn(@Valid @RequestBody StockDocumentRequest r) {
        return post(EDocumentType.CUSTOMER_RETURN, r.storeId(), null, r.externalReference(), r.note(), r.lines());
    }

    @PostMapping("/consumptions")
    @PreAuthorize(StockPermissions.Has.CONSUMPTION_POST)
    public ResponseEntity<DocumentCreatedResponse> consumption(@Valid @RequestBody StockDocumentRequest r) {
        return post(EDocumentType.CONSUMPTION, r.storeId(), null, r.externalReference(), r.note(), r.lines());
    }

    private ResponseEntity<DocumentCreatedResponse> post(EDocumentType type, UUID storeId, String supplierName,
                                                         String externalReference, String note,
                                                         List<? extends LineRequest> lines) {
        UUID id = operations.postDocument(new SingleStoreDocumentCommand(
                type, storeId, supplierName, externalReference, note,
                lines.stream().map(LineRequest::toInput).toList(),
                currentUser.requireCurrentStaffId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentCreatedResponse(id));
    }
}