package rw.afriteck.pms.auth.service.impl;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.ChangePasswordRequest;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;
import rw.afriteck.pms.auth.helpers.PasswordPolicy;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.auth.service.PlatformAuthService;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.UUID;

@Service
public class PlatformAuthServiceImpl implements PlatformAuthService {

    private final PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final String dummyHash;
    private static final int OPERATOR_MIN_PASSWORD_LENGTH = 12;

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

        String token = tokenProvider.generatePlatformAccessToken(operator.getId(), operator.getUsername(),
                operator.getRole().authorities(), operator.isMustChangePassword());
        return new PlatformLoginResponse(token, tokenProvider.getAccessTokenValidityMs() / 1000,
                operator.getRole().name(), operator.isMustChangePassword());
    }

    @Override
    @Transactional
    public PlatformLoginResponse changePassword(UUID operatorId, ChangePasswordRequest request) {
        PlatformOperator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform operator", operatorId));

        if (!operator.isEnabled()) {
            throw new BusinessRuleViolationException("OPERATOR_DISABLED", "This account is disabled");
        }
        if (!passwordEncoder.matches(request.currentPassword(), operator.getPasswordHash())) {
            throw new BusinessRuleViolationException("CURRENT_PASSWORD_INCORRECT", "The current password is incorrect");
        }
        PasswordPolicy.validate(request.newPassword(), operator.getUsername(), OPERATOR_MIN_PASSWORD_LENGTH);
        if (passwordEncoder.matches(request.newPassword(), operator.getPasswordHash())) {
            throw new BusinessRuleViolationException("PASSWORD_UNCHANGED", "The new password must differ from the current one");
        }

        operator.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        operator.setMustChangePassword(false);

        String token = tokenProvider.generatePlatformAccessToken(operator.getId(), operator.getUsername(),
                operator.getRole().authorities(), false);
        return new PlatformLoginResponse(token, tokenProvider.getAccessTokenValidityMs() / 1000,
                operator.getRole().name(), false);
    }
}
