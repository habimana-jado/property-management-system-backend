package rw.afriteck.pms.credit.dtos;

import rw.afriteck.pms.credit.enums.EAgingBucket;

import java.math.BigDecimal;
import java.util.Map;

public record AgingBucketTotals(
        BigDecimal current,
        BigDecimal days1To30,
        BigDecimal days31To60,
        BigDecimal days61To90,
        BigDecimal over90,
        BigDecimal total
) {
    public static AgingBucketTotals from(Map<EAgingBucket, BigDecimal> m) {
        BigDecimal current = m.getOrDefault(EAgingBucket.CURRENT, BigDecimal.ZERO);
        BigDecimal d30 = m.getOrDefault(EAgingBucket.DAYS_1_30, BigDecimal.ZERO);
        BigDecimal d60 = m.getOrDefault(EAgingBucket.DAYS_31_60, BigDecimal.ZERO);
        BigDecimal d90 = m.getOrDefault(EAgingBucket.DAYS_61_90, BigDecimal.ZERO);
        BigDecimal over = m.getOrDefault(EAgingBucket.OVER_90, BigDecimal.ZERO);
        return new AgingBucketTotals(current, d30, d60, d90, over,
                current.add(d30).add(d60).add(d90).add(over));
    }
}

