package rw.afriteck.pms.service;

import java.util.UUID;

public interface IBillNumberGeneratorService {
    String generateBillNo(UUID restaurantId, String restaurantCode);
}
