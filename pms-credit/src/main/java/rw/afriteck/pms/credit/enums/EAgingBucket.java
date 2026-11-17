package rw.afriteck.pms.credit.enums;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum EAgingBucket {
    CURRENT, DAYS_1_30, DAYS_31_60, DAYS_61_90, OVER_90;

    public static EAgingBucket of(LocalDate dueDate, LocalDate asOf) {
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, asOf);
        if (daysOverdue <= 0)  return CURRENT;
        if (daysOverdue <= 30) return DAYS_1_30;
        if (daysOverdue <= 60) return DAYS_31_60;
        if (daysOverdue <= 90) return DAYS_61_90;
        return OVER_90;
    }
}
