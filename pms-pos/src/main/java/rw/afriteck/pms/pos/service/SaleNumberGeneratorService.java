package rw.afriteck.pms.pos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.pos.model.POSSaleNumberCounter;
import rw.afriteck.pms.pos.repository.POSSaleNumberCounterRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleNumberGeneratorService {

    private final POSSaleNumberCounterRepository counterRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String nextSaleNumber(UUID hotelBranchId) {
        POSSaleNumberCounter counter = counterRepo.findForUpdate(hotelBranchId)
                .orElseGet(() -> {
                    POSSaleNumberCounter created = new POSSaleNumberCounter();
                    created.setHotelBranchId(hotelBranchId);
                    return counterRepo.save(created);
                });

        long next = counter.incrementAndGet();
        return "SALE-%06d".formatted(next);
    }
}
