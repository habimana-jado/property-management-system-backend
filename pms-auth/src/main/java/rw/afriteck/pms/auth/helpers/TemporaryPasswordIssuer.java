package rw.afriteck.pms.auth.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.model.User;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TemporaryPasswordIssuer {

    private final PasswordEncoder passwordEncoder;

    /** Replaces the password with a random temporary one that must be changed at the next login. */
    public String issueFor(User user) {
        String temporary = TemporaryPasswords.generate();
        user.setPasswordHash(passwordEncoder.encode(temporary));
        user.setMustChangePassword(true);
        user.setPasswordChangedAt(Instant.now());
        return temporary;
    }

    public String issueFor(PlatformOperator operator) {
        String temporary = TemporaryPasswords.generate();
        operator.setPasswordHash(passwordEncoder.encode(temporary));
        operator.setMustChangePassword(true);
        return temporary;
    }
}
