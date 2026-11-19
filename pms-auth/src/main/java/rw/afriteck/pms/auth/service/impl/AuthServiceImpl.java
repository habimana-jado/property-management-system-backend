package rw.afriteck.pms.auth.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.LoginResponse;
import rw.afriteck.pms.auth.dtos.RefreshTokenRequest;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;
import rw.afriteck.pms.auth.model.Permission;
import rw.afriteck.pms.auth.model.Role;
import rw.afriteck.pms.auth.model.User;
import rw.afriteck.pms.auth.repository.UserRepo;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.auth.service.AuthService;
import rw.afriteck.pms.common.tenant.HotelDirectory;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepository;
    private final JwtTokenProvider tokenProvider;
    private final HotelDirectory hotelDirectory;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Delegates to CustomUserDetailsService + the configured PasswordEncoder;
        // throws BadCredentialsException / DisabledException / LockedException
        // (mapped to 401) on failure.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("User not found", request.username()));

        requireActiveHotel(user.getStaff().getHotelId());

        user.setLastLoginAt(Instant.now()); // dirty checking persists this

        return issueTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshTokenRequest request) {
        if (!tokenProvider.isValid(request.refreshToken())) {
            throw new BusinessRuleViolationException("INVALID_TOKEN","Invalid or expired refresh token");
        }
        Claims claims = tokenProvider.parseClaims(request.refreshToken());
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BusinessRuleViolationException("INVALID_TOKEN","Token is not a refresh token");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", userId));

        if (!user.isEnabled() || user.isAccountLocked()) {
            throw new BusinessRuleViolationException("ACCOUNT_LOCKED", "Account is disabled or locked");
        }

        requireActiveHotel(user.getStaff().getHotelId());

        return issueTokens(user);
    }

    private LoginResponse issueTokens(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissionCodes = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        UUID hotelId = user.getStaff().getHotelId();
        String accessToken = tokenProvider.generateAccessToken(
                user.getId(), user.getStaff().getId(), user.getUsername(), hotelId,
                user.getStaff().getHotelBranchId(), roleNames, permissionCodes);

        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken, tokenProvider.getAccessTokenValidityMs() / 1000);
    }

    private void requireActiveHotel(UUID hotelId) {
        if (hotelId == null) {
            throw new ForbiddenException("NO_HOTEL", "This account is not attached to a hotel");
        }
        if (!hotelDirectory.isHotelActive(hotelId)) {
            throw new ForbiddenException("HOTEL_SUSPENDED", "This hotel's account is not active");
        }
    }

}
