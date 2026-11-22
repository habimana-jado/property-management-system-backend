package rw.afriteck.pms.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import rw.afriteck.pms.auth.dtos.PlatformPrincipal;
import rw.afriteck.pms.auth.dtos.TemporaryPasswordResponse;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.helpers.TemporaryPasswordIssuer;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;
import rw.afriteck.pms.auth.service.impl.PlatformOperatorServiceImpl;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ResourceNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformOperatorServiceImplResetTest {

    @Mock
    PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private PlatformOperatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlatformOperatorServiceImpl(operatorRepo, encoder, new TemporaryPasswordIssuer(encoder));
    }

    @AfterEach
    void cleanUp() { SecurityContextHolder.clearContext(); }

    private PlatformOperator operator(EPlatformRole role) {
        PlatformOperator o = new PlatformOperator();
        o.setId(UUID.randomUUID());
        o.setUsername("op-" + role.name().toLowerCase());
        o.setPasswordHash(encoder.encode("old-password-12"));
        o.setRole(role);
        return o;
    }

    private void signInAs(PlatformOperator operator) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new PlatformPrincipal(operator.getId(), operator.getUsername()), null,
                operator.getRole().authorities().stream().map(SimpleGrantedAuthority::new).toList()));
    }

    @Test
    void resetsAnotherOperatorAndForcesAChange() {
        PlatformOperator admin = operator(EPlatformRole.PLATFORM_ADMIN);
        PlatformOperator support = operator(EPlatformRole.PLATFORM_SUPPORT);
        signInAs(admin);
        when(operatorRepo.findById(support.getId())).thenReturn(Optional.of(support));

        TemporaryPasswordResponse response = service.resetPassword(support.getId());

        assertThat(encoder.matches(response.temporaryPassword(), support.getPasswordHash())).isTrue();
        assertThat(encoder.matches("old-password-12", support.getPasswordHash())).isFalse();
        assertThat(support.isMustChangePassword()).isTrue();
    }

    @Test
    void anAdminCannotResetThemselves() {
        PlatformOperator admin = operator(EPlatformRole.PLATFORM_ADMIN);
        signInAs(admin);
        when(operatorRepo.findById(admin.getId())).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.resetPassword(admin.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);

        assertThat(encoder.matches("old-password-12", admin.getPasswordHash())).isTrue();
    }

    @Test
    void anUnknownOperatorIsNotFound() {
        signInAs(operator(EPlatformRole.PLATFORM_ADMIN));
        UUID missing = UUID.randomUUID();
        when(operatorRepo.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(missing)).isInstanceOf(ResourceNotFoundException.class);
    }
}
