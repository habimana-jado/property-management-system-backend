package rw.afriteck.pms.stock.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.StockRuleViolationException;
import rw.afriteck.pms.stock.internal.enums.EUnitGroup;
import rw.afriteck.pms.stock.internal.model.ItemPackaging;
import rw.afriteck.pms.stock.internal.model.StockItem;
import rw.afriteck.pms.stock.internal.model.UnitOfMeasure;
import rw.afriteck.pms.stock.internal.repository.ItemPackagingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UnitOfConversionService {

    private final ItemPackagingRepository packagings;

    /** Order: same unit -> item packaging -> weight/volume group factor -> reject. */
    public BigDecimal toBase(StockItem item, UnitOfMeasure unit, BigDecimal quantity) {
        UnitOfMeasure base = item.getBaseUnit();
        if (unit.getId().equals(base.getId())) {
            return quantity.setScale(4, RoundingMode.HALF_UP);
        }
        Optional<ItemPackaging> pack = packagings.findByItemIdAndUnitId(item.getId(), unit.getId());
        if (pack.isPresent()) {
            return quantity.multiply(pack.get().getQuantityInBaseUnit()).setScale(4, RoundingMode.HALF_UP);
        }
        if (unit.getUnitGroup() == base.getUnitGroup() && unit.getUnitGroup() != EUnitGroup.COUNT) {
            return quantity.multiply(unit.getFactorToGroupBase())
                    .divide(base.getFactorToGroupBase(), 4, RoundingMode.HALF_UP);
        }
        throw new StockRuleViolationException("No conversion from " + unit.getCode()
                + " to " + base.getCode() + " for item " + item.getCode());
    }
}
