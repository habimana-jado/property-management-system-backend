package rw.afriteck.pms.auth.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlatformOperatorBootstrap implements ApplicationRunner {

    private final PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${pms.platform.bootstrap.username:}")
    private String username;
    @Value("${pms.platform.bootstrap.password:}")
    private String password;
    @Value("${pms.platform.bootstrap.force-reset:false}")
    private boolean forceReset;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (username.isBlank() || password.isBlank()) {
            return;
        }

        Optional<PlatformOperator> existing = operatorRepo.findByUsername(username.trim());
        if (existing.isPresent()) {
            if (forceReset) {
                requireStrongBootstrapPassword();
                PlatformOperator operator = existing.get();
                operator.setPasswordHash(passwordEncoder.encode(password));
                operator.setMustChangePassword(true);
                operator.setEnabled(true);
                operator.setRole(EPlatformRole.PLATFORM_ADMIN);
                log.warn("Bootstrap operator '{}' was reset by force-reset. Remove the bootstrap credentials "
                        + "and the force-reset flag from the environment now.", operator.getUsername());
            }
            return;
        }
        if (operatorRepo.count() > 0) {
            return;   // seeding only happens on a first run
        }

        requireStrongBootstrapPassword();
        PlatformOperator operator = new PlatformOperator();
        operator.setUsername(username.trim());
        operator.setFullName("Platform Administrator");
        operator.setPasswordHash(passwordEncoder.encode(password));
        operator.setRole(EPlatformRole.PLATFORM_ADMIN);
        operator.setMustChangePassword(true);
        operatorRepo.save(operator);
        log.warn("Bootstrap platform administrator '{}' created. Remove the bootstrap credentials from the environment now.",
                operator.getUsername());
    }

    private void requireStrongBootstrapPassword() {
        if (password.length() < 12) {
            throw new IllegalStateException("Bootstrap platform password must be at least 12 characters");
        }
    }
}
