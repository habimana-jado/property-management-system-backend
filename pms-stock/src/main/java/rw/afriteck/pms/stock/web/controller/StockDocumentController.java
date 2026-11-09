package rw.afriteck.pms.stock.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.afriteck.pms.stock.api.StockOperations;
import rw.afriteck.pms.stock.api.command.SingleStoreDocumentCommand;
import rw.afriteck.pms.stock.api.enums.EDocumentType;
import rw.afriteck.pms.stock.web.dtos.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock")
@RequiredArgsConstructor
public class StockDocumentController {

    private final StockOperations operations;

    @PostMapping("/receipts")
    public ResponseEntity<DocumentCreatedResponse> receipt(@Valid @RequestBody ReceiptRequest r) {
        return post(EDocumentType.RECEIPT, r.storeId(), r.supplierName(), r.externalReference(), r.note(), r.lines());
    }

    @PostMapping("/opening-balances")
    public ResponseEntity<DocumentCreatedResponse> openingBalance(@Valid @RequestBody StockDocumentRequest r) {
        return post(EDocumentType.OPENING_BALANCE, r.storeId(), null, r.externalReference(), r.note(), r.lines());
    }

    @PostMapping("/adjustments")
    public ResponseEntity<DocumentCreatedResponse> adjustment(@Valid @RequestBody AdjustmentRequest r) {
        return post(EDocumentType.ADJUSTMENT, r.storeId(), null, r.externalReference(), r.reason(), r.lines());
    }

    @PostMapping("/write-offs")
    public ResponseEntity<DocumentCreatedResponse> writeOff(@Valid @RequestBody ReasonedDocumentRequest r) {
        return post(EDocumentType.WRITE_OFF, r.storeId(), null, r.externalReference(), r.reason(), r.lines());
    }

    @PostMapping("/supplier-returns")
    public ResponseEntity<DocumentCreatedResponse> supplierReturn(@Valid @RequestBody ReasonedDocumentRequest r) {
        return post(EDocumentType.SUPPLIER_RETURN, r.storeId(), null, r.externalReference(), r.reason(), r.lines());
    }

    @PostMapping("/customer-returns")
    public ResponseEntity<DocumentCreatedResponse> customerReturn(@Valid @RequestBody StockDocumentRequest r) {
        return post(EDocumentType.CUSTOMER_RETURN, r.storeId(), null, r.externalReference(), r.note(), r.lines());
    }

    @PostMapping("/consumptions")
    public ResponseEntity<DocumentCreatedResponse> consumption(@Valid @RequestBody StockDocumentRequest r) {
        return post(EDocumentType.CONSUMPTION, r.storeId(), null, r.externalReference(), r.note(), r.lines());
    }

    private ResponseEntity<DocumentCreatedResponse> post(EDocumentType type, UUID storeId, String supplierName,
                                                         String externalReference, String note,
                                                         List<? extends LineRequest> lines) {
        UUID id = operations.postDocument(new SingleStoreDocumentCommand(
                type, storeId, supplierName, externalReference, note,
                lines.stream().map(LineRequest::toInput).toList(),
                null));
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentCreatedResponse(id));
    }
}
