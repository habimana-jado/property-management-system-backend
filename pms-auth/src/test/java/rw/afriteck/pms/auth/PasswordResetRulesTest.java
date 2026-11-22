package rw.afriteck.pms.auth;

import org.junit.jupiter.api.Test;
import rw.afriteck.pms.auth.helpers.PasswordResetRules;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ForbiddenException;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetRulesTest {

    private final UUID requester = UUID.randomUUID();
    private final UUID target = UUID.randomUUID();

    @Test
    void youCannotResetYourself() {
        assertThatThrownBy(() -> PasswordResetRules.requireAllowed(requester, requester, Set.of("A"), Set.of()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void aMoreRestrictedUserCannotResetAMorePrivilegedOne() {
        assertThatThrownBy(() -> PasswordResetRules.requireAllowed(requester, target,
                Set.of("STAFF_MANAGE"), Set.of("STAFF_MANAGE", "SALE_REFUND")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void aUserMayResetSomeoneWhosePermissionsTheyAllHold() {
        assertThatCode(() -> PasswordResetRules.requireAllowed(requester, target,
                Set.of("STAFF_MANAGE", "SALE_REFUND", "SALE_CREATE"), Set.of("SALE_CREATE")))
                .doesNotThrowAnyException();
    }

    @Test
    void aUserWithNoPermissionsAtAllCanBeReset() {
        assertThatCode(() -> PasswordResetRules.requireAllowed(requester, target, Set.of("STAFF_MANAGE"), Set.of()))
                .doesNotThrowAnyException();
    }
}