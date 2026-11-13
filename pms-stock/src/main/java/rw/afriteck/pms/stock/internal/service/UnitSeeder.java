package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.stock.api.enums.EUnitGroup;
import rw.afriteck.pms.stock.internal.model.UnitOfMeasure;
import rw.afriteck.pms.stock.internal.repository.UnitOfMeasureRepository;

import java.math.BigDecimal;

/**
 * Inserts the system units that are missing. Safe to run on every start.
 */
@Component
@RequiredArgsConstructor
class UnitSeeder implements ApplicationRunner {

    private final UnitOfMeasureRepository units;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed("G", "Gram", EUnitGroup.WEIGHT, "1");
        seed("KG", "Kilogram", EUnitGroup.WEIGHT, "1000");
        seed("ML", "Millilitre", EUnitGroup.VOLUME, "1");
        seed("L", "Litre", EUnitGroup.VOLUME, "1000");
        seed("PCS", "Piece", EUnitGroup.COUNT, "1");
        // packaging units: what one of them contains is set per item
        seed("CARTON", "Carton", EUnitGroup.COUNT, "1");
        seed("CRATE", "Crate", EUnitGroup.COUNT, "1");
        seed("BOTTLE", "Bottle", EUnitGroup.COUNT, "1");
        seed("PACK", "Pack", EUnitGroup.COUNT, "1");
    }

    private void seed(String code, String name, EUnitGroup group, String factor) {
        if (units.existsByCode(code)) return;
        UnitOfMeasure u = new UnitOfMeasure();
        u.setCode(code);
        u.setName(name);
        u.setUnitGroup(group);
        u.setFactorToGroupBase(new BigDecimal(factor));
        units.save(u);
    }
}
