package rw.afriteck.pms.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.afriteck.pms.restaurant.dtos.RecordPaymentRequest;
import rw.afriteck.pms.restaurant.dtos.CompBillRequest;
import rw.afriteck.pms.restaurant.dtos.TableBillResponse;
import rw.afriteck.pms.restaurant.dtos.VoidItemRequest;
import rw.afriteck.pms.restaurant.security.RestaurantPermissions;
import rw.afriteck.pms.restaurant.service.OrderService;
import rw.afriteck.pms.restaurant.service.TableBillService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pms/restaurant/table-bills")
@RequiredArgsConstructor
public class TableBillController {

    private final OrderService orderService;
    private final TableBillService tableBillService;

    @PostMapping("/{itemId}/void")
    @PreAuthorize(RestaurantPermissions.Has.BILL_VOID_ITEM)
    public ResponseEntity<TableBillResponse> voidItemOrder(
            @PathVariable UUID itemId,
            @Valid @RequestBody VoidItemRequest request) {
        TableBillResponse response = orderService.voidItem(itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{tableBillId}/payments")
    @PreAuthorize(RestaurantPermissions.Has.BILL_PAY)
    public ResponseEntity<TableBillResponse> recordPayment(
            @PathVariable UUID tableBillId,
            @Valid @RequestBody RecordPaymentRequest request) {
        TableBillResponse response = tableBillService.recordPayment(tableBillId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{tableBillId}/comp")
    @PreAuthorize(RestaurantPermissions.Has.BILL_COMP)
    public ResponseEntity<TableBillResponse> compBill(
            @PathVariable UUID tableBillId,
            @Valid @RequestBody CompBillRequest request) {
        TableBillResponse response = tableBillService.compBill(tableBillId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
