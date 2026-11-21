package rw.afriteck.pms.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rw.afriteck.pms.auth.helpers.LoginNames;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginNamesTest {

    @ParameterizedTest
    @CsvSource({
            "LKS-cashier1,   LKS-cashier1",
            "lks-CashIer1,   LKS-cashier1",
            "LKS-alice.admin, LKS-alice.admin",
            "LKS-a-b-c,      LKS-a-b-c"          // only the first dash ends the hotel code
    })
    void normalizesWhatIsTypedAtLogin(String typed, String expected) {
        assertThat(LoginNames.normalizeLogin(typed)).contains(expected);
    }

    @Test
    void ignoresSurroundingWhitespace() {
        assertThat(LoginNames.normalizeLogin("  lks-cashier1 \t")).contains("LKS-cashier1");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"cashier1", "-cashier1", "LKS-", "   "})
    void rejectsAnythingNotInHotelCodeUsernameForm(String typed) {
        assertThat(LoginNames.normalizeLogin(typed)).isEmpty();
    }

    @Test
    void composeUppercasesTheCodeAndLowercasesTheName() {
        assertThat(LoginNames.compose("lks", "  Cashier1 ")).isEqualTo("LKS-cashier1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "has space", ".leading", "UPPER CASE", ""})
    void composeRejectsInvalidUsernames(String username) {
        assertThatThrownBy(() -> LoginNames.compose("LKS", username))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
