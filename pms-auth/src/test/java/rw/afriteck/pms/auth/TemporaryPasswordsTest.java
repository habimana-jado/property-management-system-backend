package rw.afriteck.pms.auth;

import org.junit.jupiter.api.Test;
import rw.afriteck.pms.auth.helpers.PasswordPolicy;
import rw.afriteck.pms.auth.helpers.TemporaryPasswords;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TemporaryPasswordsTest {

    @Test
    void generatedPasswordsAreReadableCompliantAndDistinct() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            String password = TemporaryPasswords.generate();
            assertThat(password).matches("[A-HJ-NP-Za-km-z2-9]{12}");
            assertThatCode(() -> PasswordPolicy.validate(password, "LKS-cashier1")).doesNotThrowAnyException();
            seen.add(password);
        }
        assertThat(seen).hasSize(500);
    }
}
