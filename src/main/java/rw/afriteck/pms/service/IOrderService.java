package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.*;

import java.util.UUID;

public interface IOrderService {

    OrderPlacementResponse placeOrder(UUID tableId, PlaceOrderRequest tableBillItemRequest);
    TableSplitResponse splitTable(UUID sourceTableId, SplitTableRequest request);
    TableBillResponse mergeTable(UUID destinationTableId, MergeTableRequest request);
}
