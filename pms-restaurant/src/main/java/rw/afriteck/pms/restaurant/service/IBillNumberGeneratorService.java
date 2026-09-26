package rw.afriteck.pms.restaurant.service;

import java.util.UUID;

public interface IBillNumberGeneratorService {
    String generateBillNo(UUID restaurantId, String restaurantCode);
}
