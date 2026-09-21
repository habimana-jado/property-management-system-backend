package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.BillSnapshotResponse;

import java.util.UUID;

public interface ITableBillService {
    BillSnapshotResponse requestBill(UUID tableId);
}
