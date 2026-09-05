package rw.afriteck.pms.service;

import rw.afriteck.pms.dtos.PlaceOrderRequest;
import rw.afriteck.pms.dtos.TableBillItemResponse;

import java.util.UUID;

public interface IOrderService {

    TableBillItemResponse placeOrder(UUID tableId, PlaceOrderRequest tableBillItemRequest);
}
