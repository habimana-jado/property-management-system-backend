package rw.afriteck.pms.credit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rw.afriteck.pms.credit.enums.EAgingBucket;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EAgingBucketTest {

    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 17);

    @ParameterizedTest(name = "{0} days overdue -> {1}")
    @CsvSource({
            "-5,  CURRENT",     // not yet due
            "0,   CURRENT",     // due today is not overdue
            "1,   DAYS_1_30",
            "30,  DAYS_1_30",
            "31,  DAYS_31_60",
            "60,  DAYS_31_60",
            "61,  DAYS_61_90",
            "90,  DAYS_61_90",
            "91,  OVER_90",
            "400, OVER_90"
    })
    void bucketsByDaysPastDueDate(long daysOverdue, EAgingBucket expected) {
        LocalDate dueDate = AS_OF.minusDays(daysOverdue);

        assertThat(EAgingBucket.of(dueDate, AS_OF)).isEqualTo(expected);
    }
}
