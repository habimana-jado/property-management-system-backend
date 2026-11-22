package rw.afriteck.pms.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.afriteck.pms.auth.dtos.ChangePasswordRequest;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.LoginResponse;
import rw.afriteck.pms.auth.dtos.RefreshTokenRequest;
import rw.afriteck.pms.auth.model.Staff;
import rw.afriteck.pms.auth.model.User;
import rw.afriteck.pms.auth.repository.UserRepo;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.auth.service.impl.AuthServiceImpl;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.tenant.HotelDirectory;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplPasswordTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserRepo userRepository;
    @Mock
    HotelDirectory hotelDirectory;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtTokenProvider tokenProvider =
            new JwtTokenProvider("0123456789-0123456789-0123456789-0123", 900_000, 900_000);
    private AuthServiceImpl service;
    private final UUID hotelId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(authenticationManager, userRepository, tokenProvider, hotelDirectory, encoder);
    }

    private User user(boolean mustChange) {
        Staff staff = new Staff();
        staff.setId(UUID.randomUUID());
        staff.setHotelId(hotelId);

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("LKS-cashier1");
        u.setStaff(staff);
        u.setRoles(new HashSet<>());
        u.setEnabled(true);
        u.setAccountLocked(false);
        u.setPasswordHash(encoder.encode("Temp-pass-1"));
        u.setMustChangePassword(mustChange);
        return u;
    }

    @Test
    void changingThePasswordClearsTheFlagAndIssuesFreshTokens() {
        User user = user(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        LoginResponse response = service.changePassword(user.getId(),
                new ChangePasswordRequest("Temp-pass-1", "N3w-secret-pw"));

        assertThat(encoder.matches("N3w-secret-pw", user.getPasswordHash())).isTrue();
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(user.getPasswordChangedAt()).isNotNull();
        assertThat(response.mustChangePassword()).isFalse();
        assertThat(tokenProvider.parseClaims(response.accessToken()).get("mustChangePassword", Boolean.class)).isFalse();
    }

    @Test
    void aWrongCurrentPasswordChangesNothing() {
        User user = user(true);
        String hashBefore = user.getPasswordHash();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(user.getId(),
                new ChangePasswordRequest("not-the-password", "N3w-secret-pw")))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(user.getPasswordHash()).isEqualTo(hashBefore);
        assertThat(user.isMustChangePassword()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "nodigitsatall", "Temp-pass-1"})   // too weak / no digit / same as current
    void aNewPasswordThatBreaksARuleIsRefused(String newPassword) {
        User user = user(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.changePassword(user.getId(),
                new ChangePasswordRequest("Temp-pass-1", newPassword)))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(user.isMustChangePassword()).isTrue();
    }

    @Test
    void refreshRejectsATokenIssuedBeforeTheLastPasswordChange() {
        User user = user(false);
        user.setPasswordChangedAt(Instant.now().plusSeconds(5));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest(tokenProvider.generateRefreshToken(user.getId()))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void refreshAcceptsATokenIssuedAfterTheLastPasswordChange() {
        User user = user(false);
        user.setPasswordChangedAt(Instant.now().minusSeconds(60));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(hotelDirectory.isHotelActive(hotelId)).thenReturn(true);

        LoginResponse response = service.refresh(new RefreshTokenRequest(tokenProvider.generateRefreshToken(user.getId())));

        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    void loginTellsTheClientWhenAPasswordChangeIsRequired() {
        User user = user(true);
        when(userRepository.findByUsername("LKS-cashier1")).thenReturn(Optional.of(user));
        when(hotelDirectory.isHotelActive(hotelId)).thenReturn(true);

        LoginResponse response = service.login(new LoginRequest("LKS-cashier1", "Temp-pass-1"));

        assertThat(response.mustChangePassword()).isTrue();
        assertThat(tokenProvider.parseClaims(response.accessToken()).get("mustChangePassword", Boolean.class)).isTrue();
    }
}
