package rw.afriteck.pms.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import rw.afriteck.pms.auth.enums.EPlatformRole;
import rw.afriteck.pms.auth.helpers.PlatformOperatorBootstrap;
import rw.afriteck.pms.auth.model.PlatformOperator;
import rw.afriteck.pms.auth.repository.PlatformOperatorRepo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformOperatorBootstrapTest {

    @Mock
    PlatformOperatorRepo operatorRepo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private PlatformOperatorBootstrap bootstrap;

    @BeforeEach
    void setUp() { bootstrap = new PlatformOperatorBootstrap(operatorRepo, encoder); }

    private void configure(String username, String password, boolean forceReset) {
        ReflectionTestUtils.setField(bootstrap, "username", username);
        ReflectionTestUtils.setField(bootstrap, "password", password);
        ReflectionTestUtils.setField(bootstrap, "forceReset", forceReset);
    }

    private PlatformOperator existing(String password) {
        PlatformOperator o = new PlatformOperator();
        o.setUsername("root");
        o.setPasswordHash(encoder.encode(password));
        o.setRole(EPlatformRole.PLATFORM_SUPPORT);
        o.setEnabled(false);
        return o;
    }

    @Test
    void createsAFlaggedAdminOnAFirstRun() {
        configure("root", "a-long-bootstrap-pw", false);
        when(operatorRepo.findByUsername("root")).thenReturn(Optional.empty());
        when(operatorRepo.count()).thenReturn(0L);

        bootstrap.run(null);

        ArgumentCaptor<PlatformOperator> captor = ArgumentCaptor.forClass(PlatformOperator.class);
        verify(operatorRepo).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(EPlatformRole.PLATFORM_ADMIN);
        assertThat(captor.getValue().isMustChangePassword()).isTrue();
        assertThat(encoder.matches("a-long-bootstrap-pw", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void doesNothingWhenOtherOperatorsAlreadyExist() {
        configure("root", "a-long-bootstrap-pw", false);
        when(operatorRepo.findByUsername("root")).thenReturn(Optional.empty());
        when(operatorRepo.count()).thenReturn(3L);

        bootstrap.run(null);

        verify(operatorRepo, never()).save(any());
    }

    @Test
    void anExistingOperatorIsUntouchedWithoutForceReset() {
        configure("root", "a-long-bootstrap-pw", false);
        PlatformOperator op = existing("their-own-password-1");
        when(operatorRepo.findByUsername("root")).thenReturn(Optional.of(op));

        bootstrap.run(null);

        assertThat(encoder.matches("their-own-password-1", op.getPasswordHash())).isTrue();
        assertThat(op.isEnabled()).isFalse();
    }

    @Test
    void forceResetRestoresAnAdminWithAFlaggedPassword() {
        configure("root", "a-long-bootstrap-pw", true);
        PlatformOperator op = existing("forgotten-password-1");
        when(operatorRepo.findByUsername("root")).thenReturn(Optional.of(op));

        bootstrap.run(null);

        assertThat(encoder.matches("a-long-bootstrap-pw", op.getPasswordHash())).isTrue();
        assertThat(op.isMustChangePassword()).isTrue();
        assertThat(op.isEnabled()).isTrue();
        assertThat(op.getRole()).isEqualTo(EPlatformRole.PLATFORM_ADMIN);
    }

    @Test
    void aWeakBootstrapPasswordStopsTheStartup() {
        configure("root", "short", false);
        when(operatorRepo.findByUsername("root")).thenReturn(Optional.empty());
        when(operatorRepo.count()).thenReturn(0L);

        assertThatThrownBy(() -> bootstrap.run(null)).isInstanceOf(IllegalStateException.class);
    }
}
