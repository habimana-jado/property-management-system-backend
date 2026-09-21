package rw.afriteck.pms.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.exception.ResourceNotFoundException;
import rw.afriteck.pms.model.BillNumberCounter;
import rw.afriteck.pms.repository.BillNumberCounterRepo;
import rw.afriteck.pms.service.IBillNumberGeneratorService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillNumberGeneratorService implements IBillNumberGeneratorService {

    private final BillNumberCounterRepo billNumberCounterRepo;

    @Override
    @Transactional
    public String generateBillNo(UUID restaurantId, String restaurantCode) {
        BillNumberCounter counter = billNumberCounterRepo.findByIdForUpdate(restaurantId)
                .orElseThrow(()->new ResourceNotFoundException("Bill Number Counter", restaurantId));

        long next = counter.getLastNumber() + 1;
        counter.setLastNumber(next);
        //No need to call .save() in this case since
        //counter is a ManagedEntity, via Hibernate behavior called dirty checking

        return String.format("%s-%06d", restaurantCode, next);
    }
}
