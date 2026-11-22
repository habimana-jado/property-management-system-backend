package rw.afriteck.pms.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rw.afriteck.pms.auth.helpers.PasswordPolicy;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {"abc12345", "Sup3rSecret", "correct-horse-battery-9"})
    void acceptsReasonablePasswords(String password) {
        assertThatCode(() -> PasswordPolicy.validate(password, "LKS-cashier1")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1", "onlyletters", "12345678", "has cashier1 inside"})
    void rejectsWeakPasswords(String password) {
        assertThatThrownBy(() -> PasswordPolicy.validate(password, "LKS-cashier1"))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void rejectsPasswordsLongerThanBcryptReads() {
        assertThatThrownBy(() -> PasswordPolicy.validate("a1".repeat(37), "LKS-cashier1"))   // 74 bytes
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void aLongerMinimumCanBeRequired() {
        assertThatThrownBy(() -> PasswordPolicy.validate("abc12345", "op1", 12))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatCode(() -> PasswordPolicy.validate("abcdefgh1234", "op1", 12)).doesNotThrowAnyException();
    }
}
