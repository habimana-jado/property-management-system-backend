package rw.afriteck.pms.auth.service.impl;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.auth.service.PlatformAuthService;

import java.time.Instant;

@Service
public class PlatformAuthServiceImpl implements PlatformAuthService {

    private final PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final String dummyHash;

    public PlatformAuthServiceImpl(PlatformOperatorRepo operatorRepo, PasswordEncoder passwordEncoder,
                                   JwtTokenProvider tokenProvider) {
        this.operatorRepo = operatorRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @Override
    @Transactional
    public PlatformLoginResponse login(LoginRequest request) {
        PlatformOperator operator = operatorRepo.findByUsername(request.username()).orElse(null);

        // always run one hash comparison so response time doesn't reveal whether the username exists
        boolean passwordOk = passwordEncoder.matches(request.password(),
                operator != null ? operator.getPasswordHash() : dummyHash);

        if (operator == null || !passwordOk || !operator.isEnabled()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        operator.setLastLoginAt(Instant.now()); // dirty checking persists this
        String token = tokenProvider.generatePlatformAccessToken(
                operator.getId(), operator.getUsername(), operator.getRole().authorities());

        return new PlatformLoginResponse(token,
                tokenProvider.getAccessTokenValidityMs() / 1000, operator.getRole().name());
    }
}
