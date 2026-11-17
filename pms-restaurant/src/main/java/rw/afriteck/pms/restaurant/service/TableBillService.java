package rw.afriteck.pms.restaurant.service;

import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.restaurant.dtos.*;

import java.util.UUID;

public interface TableBillService {
    TableBillResponse requestBill(UUID tableId);

    BillSnapshotResponse requestBillItemsCombined(UUID tableId);

    TableBillResponse recordPayment(UUID tableBillId, RecordPaymentRequest request);

    TableBillResponse compBill(UUID tableBillId, CompBillRequest request);

    TableBillResponse closeBillOnCredit(UUID tableBillId, CloseOnCreditRequest request);
}
