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
import rw.afriteck.pms.stock.api.command.TransferCommand;
import rw.afriteck.pms.stock.web.dtos.DocumentCreatedResponse;
import rw.afriteck.pms.stock.web.dtos.DocumentLineRequest;
import rw.afriteck.pms.stock.web.dtos.TransferRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/stock/transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockOperations operations;

    @PostMapping
    public ResponseEntity<DocumentCreatedResponse> transfer(@Valid @RequestBody TransferRequest r) {
        UUID id = operations.transfer(new TransferCommand(
                r.fromStoreId(), r.toStoreId(), r.purpose(), r.note(),
                r.lines().stream().map(DocumentLineRequest::toInput).toList(),
                null));
        return ResponseEntity.status(HttpStatus.CREATED).body(new DocumentCreatedResponse(id));
    }
}
