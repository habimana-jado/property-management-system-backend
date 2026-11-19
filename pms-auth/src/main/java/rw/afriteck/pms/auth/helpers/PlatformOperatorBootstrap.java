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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (operatorRepo.count() > 0 || username.isBlank() || password.isBlank()) {
            return;
        }
        if (password.length() < 6) {
            throw new IllegalStateException("Bootstrap platform password must be at least 12 characters");
        }
        PlatformOperator operator = new PlatformOperator();
        operator.setUsername(username.trim());
        operator.setFullName("Platform Administrator");
        operator.setPasswordHash(passwordEncoder.encode(password));
        operator.setRole(EPlatformRole.PLATFORM_ADMIN);
        operatorRepo.save(operator);
        log.warn("Bootstrap platform administrator '{}' created. Remove the bootstrap credentials from the environment now.",
                operator.getUsername());
    }
}
