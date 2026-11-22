package rw.afriteck.pms.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.CreatePlatformOperatorRequest;
import rw.afriteck.pms.auth.dtos.PlatformOperatorResponse;
import rw.afriteck.pms.auth.dtos.PlatformPrincipal;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.helpers.PasswordResetRules;
import rw.afriteck.pms.auth.helpers.TemporaryPasswordIssuer;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;
import rw.afriteck.pms.auth.service.PlatformOperatorService;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformOperatorServiceImpl implements PlatformOperatorService {

    private final PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder passwordEncoder;
    private final TemporaryPasswordIssuer passwordIssuer;

    @Transactional
    @Override
    public PlatformOperatorResponse create(CreatePlatformOperatorRequest request) {
        if (operatorRepo.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        PlatformOperator operator = new PlatformOperator();
        operator.setUsername(request.username());
        operator.setFullName(request.fullName());
        operator.setPasswordHash(passwordEncoder.encode(request.password()));
        operator.setMustChangePassword(true);
        operator.setRole(request.role());
        return toResponse(operatorRepo.save(operator));
    }

    @Transactional
    @Override
    public PlatformOperatorResponse setEnabled(UUID operatorId, boolean enabled) {
        PlatformOperator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform operator", operatorId));

        if (!enabled && operator.isEnabled() && operator.getRole() == EPlatformRole.PLATFORM_ADMIN
                && operatorRepo.countByRoleAndEnabledTrue(EPlatformRole.PLATFORM_ADMIN) <= 1) {
            throw new BusinessRuleViolationException("LAST_PLATFORM_ADMIN",
                    "Cannot disable the last active platform administrator");
        }
        operator.setEnabled(enabled);
        return toResponse(operator);
    }

    @Override
    @Transactional
    public TemporaryPasswordResponse resetPassword(UUID operatorId) {
        PlatformOperator target = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Platform operator", operatorId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof PlatformPrincipal requester)) {
            throw new ForbiddenException("OPERATOR_ONLY", "Only platform operators can reset operator passwords");
        }
        Set<String> requesterAuthorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        PasswordResetRules.requireAllowed(requester.operatorId(), target.getId(),
                requesterAuthorities, target.getRole().authorities());

        String temporary = passwordIssuer.issueFor(target);
        log.info("Operator password reset for {} by {}", target.getUsername(), requester.username());
        return new TemporaryPasswordResponse(target.getUsername(), temporary);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PlatformOperatorResponse> findAll() {
        return operatorRepo.findAll().stream().map(this::toResponse).toList();
    }

    private PlatformOperatorResponse toResponse(PlatformOperator o) {
        return new PlatformOperatorResponse(o.getId(), o.getUsername(), o.getFullName(),
                o.getRole(), o.isEnabled(), o.getLastLoginAt());
    }
}
