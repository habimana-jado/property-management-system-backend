package rw.afriteck.pms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import rw.afriteck.pms.dtos.*;
import rw.afriteck.pms.mapper.BillSnapshotMapper;
import rw.afriteck.pms.service.IOrderService;
import rw.afriteck.pms.service.ITableBillService;
import rw.afriteck.pms.service.ITableMasterService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/tables")
@RequiredArgsConstructor
public class TableMasterController {
    private final ITableMasterService tableMasterService;
    private final IOrderService orderService;
    private final ITableBillService tableBillService;

    @PostMapping("/{tableId}/orders")
    public ResponseEntity<TableBillItemResponse> placeOrder(
            @PathVariable UUID tableId,
            @Valid @RequestBody PlaceOrderRequest request) {
        TableBillItemResponse response = orderService.placeOrder(tableId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{tableId}/bills")
    public ResponseEntity<BillSnapshotResponse> requestBill(@PathVariable UUID tableId) {
        BillSnapshotResponse response = tableBillService.requestBill(tableId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<TableMasterResponse> register(@Valid @RequestBody CreateTableMasterRequest tableMasterRequest) {
        TableMasterResponse tableMasterResponse = tableMasterService.register(tableMasterRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tableMasterResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(tableMasterResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TableMasterResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody CreateTableMasterRequest tableMasterRequest) {
        TableMasterResponse tableMasterResponse = tableMasterService.update(id, tableMasterRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tableMasterResponse.id())
                .toUri();
        return ResponseEntity.created(location).body(tableMasterResponse);
    }

    @GetMapping
    public ResponseEntity<Page<TableMasterResponse>> findAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(tableMasterService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableMasterResponse> findOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(tableMasterService.findOne(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<TableMasterResponse> activate(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(tableMasterService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TableMasterResponse> deactivate(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(tableMasterService.deactivate(id));
    }


}
