package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.PlaceOrderRequest;
import rw.afriteck.pms.dtos.SplitTableRequest;
import rw.afriteck.pms.dtos.TableBillItemResponse;
import rw.afriteck.pms.dtos.TableSplitResponse;

import java.util.UUID;

public interface IOrderService {

    TableBillItemResponse placeOrder(UUID tableId, PlaceOrderRequest tableBillItemRequest);
    TableSplitResponse splitTable(UUID sourceTableId, SplitTableRequest request);
}
