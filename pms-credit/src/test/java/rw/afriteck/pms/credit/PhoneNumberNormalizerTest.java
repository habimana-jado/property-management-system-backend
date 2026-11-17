package rw.afriteck.pms.credit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.credit.utils.PhoneNumberNormalizer;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberNormalizerTest {

    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer("250");

    @ParameterizedTest(name = "[{index}] \"{0}\" -> {1}")
    @CsvSource({
            "0788123456,          +250788123456",  // local format with leading zero
            "0788 123 456,        +250788123456",  // spaces
            "0788-123-456,        +250788123456",  // dashes
            "(0788) 123-456,      +250788123456",  // brackets
            "078.812.3456,        +250788123456",  // dots
            "788123456,           +250788123456",  // no leading zero, no country code
            "250788123456,        +250788123456",  // country code without plus
            "00250788123456,      +250788123456",  // international 00 prefix
            "+250788123456,       +250788123456",  // already normalized
            "+250 788 123 456,    +250788123456",  // normalized with spaces
            "+254712345678,       +254712345678"   // explicit foreign code is kept, not overwritten
    })
    void normalizesCommonFormats(String raw, String expected) {
        assertThat(normalizer.normalize(raw)).isEqualTo(expected);
    }

    @Test
    void differentWritingsOfTheSameNumberCollapseToOneValue() {
        // this is the whole point: the unique constraint only works if these all match
        Set<String> results = Stream.of(
                        "0788123456", "0788 123 456", "788123456", "250788123456",
                        "+250788123456", "00250788123456", "(0788) 123-456")
                .map(normalizer::normalize)
                .collect(Collectors.toSet());

        assertThat(results).containsExactly("+250788123456");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0788123456", "788123456", "250788123456", "00250788123456", "+250 788 123 456"})
    void normalizingTwiceGivesTheSameResult(String raw) {
        String once = normalizer.normalize(raw);

        assertThat(normalizer.normalize(once)).isEqualTo(once);
    }

    @Test
    void ignoresSurroundingWhitespace() {
        assertThat(normalizer.normalize("  0788123456 \t")).isEqualTo("+250788123456");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ",                       // blank
            "abc",                       // no digits
            "0",                         // nothing after the trunk prefix
            "123",                       // too short
            "+0788123456",               // country code can't start with 0
            "078812345678901234567",     // too long (over 15 digits)
            "0788-12x456",               // stray letter
            "+250788123456 ext 5"        // trailing text
    })
    void rejectsInvalidInput(String raw) {
        assertThatThrownBy(() -> normalizer.normalize(raw))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void usesTheConfiguredDefaultCountryCode() {
        PhoneNumberNormalizer other = new PhoneNumberNormalizer("254");

        assertThat(other.normalize("0712345678")).isEqualTo("+254712345678");
        assertThat(other.normalize("712345678")).isEqualTo("+254712345678");
        assertThat(other.normalize("+250788123456")).isEqualTo("+250788123456"); // explicit code wins
    }
}
