package rw.afriteck.pms.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.afriteck.pms.auth.helpers.TemporaryPasswordIssuer;
import rw.afriteck.pms.auth.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class TemporaryPasswordIssuerTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final TemporaryPasswordIssuer issuer = new TemporaryPasswordIssuer(encoder);

    @Test
    void replacesThePasswordAndForcesAChange() {
        User user = new User();
        user.setPasswordHash(encoder.encode("old-password-1"));

        String temporary = issuer.issueFor(user);

        assertThat(encoder.matches(temporary, user.getPasswordHash())).isTrue();
        assertThat(encoder.matches("old-password-1", user.getPasswordHash())).isFalse();
        assertThat(user.isMustChangePassword()).isTrue();
        assertThat(user.getPasswordChangedAt()).isNotNull();
    }
}
