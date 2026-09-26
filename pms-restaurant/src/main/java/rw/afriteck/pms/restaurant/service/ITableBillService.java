package rw.afriteck.pms.restaurant.service;

import rw.afriteck.pms.payment.dtos.RecordPaymentRequest;
import rw.afriteck.pms.restaurant.dtos.BillSnapshotResponse;
import rw.afriteck.pms.restaurant.dtos.CompBillRequest;
import rw.afriteck.pms.restaurant.dtos.TableBillResponse;

import java.util.UUID;

public interface ITableBillService {
    TableBillResponse requestBill(UUID tableId);

    BillSnapshotResponse requestBillItemsCombined(UUID tableId);

    TableBillResponse recordPayment(UUID tableBillId, RecordPaymentRequest request);

    TableBillResponse compBill(UUID tableBillId, CompBillRequest request);
}
