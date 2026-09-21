package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.BillSnapshotResponse;
import rw.afriteck.pms.dtos.TableBillResponse;

import java.util.UUID;

public interface ITableBillService {
    TableBillResponse requestBill(UUID tableId);
    BillSnapshotResponse requestBillItemsCombined(UUID tableId);
}
