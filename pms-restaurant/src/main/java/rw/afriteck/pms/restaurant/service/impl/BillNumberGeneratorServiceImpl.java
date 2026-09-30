package rw.afriteck.pms.restaurant.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.restaurant.model.BillNumberCounter;
import rw.afriteck.pms.restaurant.repository.BillNumberCounterRepo;
import rw.afriteck.pms.restaurant.service.BillNumberGeneratorService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillNumberGeneratorServiceImpl implements BillNumberGeneratorService {

    private final BillNumberCounterRepo billNumberCounterRepo;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateBillNo(UUID restaurantId, String restaurantCode) {
        BillNumberCounter counter = billNumberCounterRepo.findByIdForUpdate(restaurantId)
                .orElseThrow(()->new ResourceNotFoundException("Bill Number Counter", restaurantId));

        long next = counter.getLastNumber() + 1;
        counter.setLastNumber(next);

        return String.format("%s-%06d", restaurantCode, next);
    }
}
