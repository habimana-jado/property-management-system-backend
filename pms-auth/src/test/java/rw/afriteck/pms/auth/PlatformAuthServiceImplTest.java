package rw.afriteck.pms.auth;

import io.jsonwebtoken.Claims;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.afriteck.pms.auth.dtos.LoginRequest;
import rw.afriteck.pms.auth.dtos.PlatformLoginResponse;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;
import rw.afriteck.pms.auth.security.JwtTokenProvider;
import rw.afriteck.pms.auth.service.impl.PlatformAuthServiceImpl;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformAuthServiceImplTest {

    @Mock
    PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // low cost: fast tests
    private final JwtTokenProvider tokenProvider =
            new JwtTokenProvider("0123456789-0123456789-0123456789-0123", 900_000, 900_000);
    private PlatformAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlatformAuthServiceImpl(operatorRepo, encoder, tokenProvider);
    }

    private PlatformOperator operator(boolean enabled) {
        PlatformOperator o = new PlatformOperator();
        o.setId(UUID.randomUUID());
        o.setUsername("op1");
        o.setPasswordHash(encoder.encode("correct-password-1"));
        o.setRole(EPlatformRole.PLATFORM_ADMIN);
        o.setEnabled(enabled);
        return o;
    }

    @Test
    void issuesAPlatformTokenWithRoleAuthorities_andNoHotel() {
        when(operatorRepo.findByUsername("op1")).thenReturn(Optional.of(operator(true)));

        PlatformLoginResponse response = service.login(new LoginRequest("op1", "correct-password-1"));

        Claims claims = tokenProvider.parseClaims(response.accessToken());
        assertThat(claims.get("type", String.class)).isEqualTo("platform-access");
        assertThat(claims.get("hotelId")).isNull();
        assertThat((Object) claims.get("authorities", Collection.class))
                .asInstanceOf(InstanceOfAssertFactories.COLLECTION)
                .contains("PLATFORM_MANAGE_HOTELS");
    }

    @Test
    void rejectsAWrongPassword() {
        when(operatorRepo.findByUsername("op1")).thenReturn(Optional.of(operator(true)));

        assertThatThrownBy(() -> service.login(new LoginRequest("op1", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAnUnknownUsername_withTheSameError() {
        when(operatorRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("ghost", "whatever-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsADisabledOperator_evenWithTheRightPassword() {
        when(operatorRepo.findByUsername("op1")).thenReturn(Optional.of(operator(false)));

        assertThatThrownBy(() -> service.login(new LoginRequest("op1", "correct-password-1")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
