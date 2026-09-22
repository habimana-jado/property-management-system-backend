package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.BillSnapshotResponse;
import rw.afriteck.pms.dtos.CompBillRequest;
import rw.afriteck.pms.dtos.RecordPaymentRequest;
import rw.afriteck.pms.dtos.TableBillResponse;

import java.util.UUID;

public interface ITableBillService {
    TableBillResponse requestBill(UUID tableId);

    BillSnapshotResponse requestBillItemsCombined(UUID tableId);

    TableBillResponse recordPayment(UUID tableBillId, RecordPaymentRequest request);

    TableBillResponse compBill(UUID tableBillId, CompBillRequest request);
}
