package rw.afriteck.pms.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.CreatePlatformOperatorRequest;
import rw.afriteck.pms.auth.dtos.PlatformOperatorResponse;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;
import rw.afriteck.pms.auth.service.PlatformOperatorService;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.DuplicateResourceException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformOperatorServiceImpl implements PlatformOperatorService {

    private final PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_OPERATORS')")
    @Override
    public PlatformOperatorResponse create(CreatePlatformOperatorRequest request) {
        if (operatorRepo.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already taken: " + request.username());
        }
        PlatformOperator operator = new PlatformOperator();
        operator.setUsername(request.username());
        operator.setFullName(request.fullName());
        operator.setPasswordHash(passwordEncoder.encode(request.password()));
        operator.setRole(request.role());
        return toResponse(operatorRepo.save(operator));
    }

    @Transactional
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_OPERATORS')")
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

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PLATFORM_MANAGE_OPERATORS')")
    @Override
    public List<PlatformOperatorResponse> findAll() {
        return operatorRepo.findAll().stream().map(this::toResponse).toList();
    }

    private PlatformOperatorResponse toResponse(PlatformOperator o) {
        return new PlatformOperatorResponse(o.getId(), o.getUsername(), o.getFullName(),
                o.getRole(), o.isEnabled(), o.getLastLoginAt());
    }
}
