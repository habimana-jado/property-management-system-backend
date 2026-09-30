package rw.afriteck.pms.pos.service;

import rw.afriteck.pms.pos.dtos.*;

import java.util.UUID;

public interface POSSaleService {

    POSSaleResponse create(CreateSaleRequest request);

    POSSaleResponse addItem(UUID saleId, AddSaleItemRequest request);

    POSSaleResponse updateItemQuantity(UUID saleId, UUID itemId, UpdateSaleItemQuantityRequest request);

    POSSaleResponse removeItem(UUID saleId, UUID itemId);

    POSSaleResponse hold(UUID saleId);

    POSSaleResponse resume(UUID saleId);

    POSSaleResponse cancel(UUID saleId);

    POSSaleResponse complete(UUID saleId, CompleteSaleRequest request);

    POSSaleResponse findById(UUID saleId);
}
