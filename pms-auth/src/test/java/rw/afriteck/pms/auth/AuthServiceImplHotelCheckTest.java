package rw.afriteck.pms.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.LoginResponse;
import rw.afriteck.pms.auth.dtos.RefreshTokenRequest;
import rw.afriteck.pms.auth.model.Staff;
import rw.afriteck.pms.auth.model.User;
import rw.afriteck.pms.auth.repository.UserRepo;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.auth.service.impl.AuthServiceImpl;
import rw.afriteck.pms.common.exception.ForbiddenException;
import rw.afriteck.pms.common.tenant.HotelDirectory;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplHotelCheckTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserRepo userRepository;
    @Mock
    HotelDirectory hotelDirectory;
    @Mock
    PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider =
            new JwtTokenProvider("0123456789-0123456789-0123456789-0123", 900_000, 900_000);
    private AuthServiceImpl service;

    private final UUID hotelId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(authenticationManager, userRepository, tokenProvider, hotelDirectory, passwordEncoder);
    }

    private User userOf(UUID staffHotelId) {
        Staff staff = new Staff();
        staff.setId(UUID.randomUUID());
        staff.setHotelId(staffHotelId);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("LKS-cashier1");
        user.setStaff(staff);
        user.setRoles(new HashSet<>());
        user.setEnabled(true);
        user.setAccountLocked(false);
        return user;
    }

    @Test
    void loginIsRefusedWhenTheHotelIsSuspended() {
        User user = userOf(hotelId);
        when(userRepository.findByUsername("LKS-cashier1")).thenReturn(Optional.of(user));
        when(hotelDirectory.isHotelActive(hotelId)).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("LKS-cashier1", "secret")))
                .isInstanceOf(ForbiddenException.class);

        assertThat(user.getLastLoginAt()).isNull();   // no side effects for a refused login
    }

    @Test
    void loginIssuesATokenCarryingTheHotelWhenTheHotelIsActive() {
        when(userRepository.findByUsername("LKS-cashier1")).thenReturn(Optional.of(userOf(hotelId)));
        when(hotelDirectory.isHotelActive(hotelId)).thenReturn(true);

        LoginResponse response = service.login(new LoginRequest("LKS-cashier1", "secret"));

        Claims claims = tokenProvider.parseClaims(response.accessToken());
        assertThat(claims.get("hotelId", String.class)).isEqualTo(hotelId.toString());
    }

    @Test
    void loginIsRefusedWhenTheStaffHasNoHotel() {
        when(userRepository.findByUsername("LKS-cashier1")).thenReturn(Optional.of(userOf(null)));

        assertThatThrownBy(() -> service.login(new LoginRequest("LKS-cashier1", "secret")))
                .isInstanceOf(ForbiddenException.class);

        verifyNoInteractions(hotelDirectory);
    }

    @Test
    void refreshIsRefusedWhenTheHotelWasSuspendedAfterLogin() {
        User user = userOf(hotelId);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(hotelDirectory.isHotelActive(hotelId)).thenReturn(false);

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest(refreshToken)))
                .isInstanceOf(ForbiddenException.class);
    }
}
