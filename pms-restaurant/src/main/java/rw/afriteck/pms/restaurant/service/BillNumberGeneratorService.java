package rw.afriteck.pms.restaurant.service;

import java.util.UUID;

public interface BillNumberGeneratorService {
    String generateBillNo(UUID restaurantId, String restaurantCode);
}
