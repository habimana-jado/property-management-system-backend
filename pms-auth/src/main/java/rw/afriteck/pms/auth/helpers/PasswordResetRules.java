package rw.afriteck.pms.auth.helpers;

import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.common.exception.ForbiddenException;

import java.util.Set;
import java.util.UUID;

public final class PasswordResetRules {

    private PasswordResetRules() {}

    public static void requireAllowed(UUID requesterUserId, UUID targetUserId,
                                      Set<String> requesterAuthorities, Set<String> targetPermissions) {
        if (requesterUserId.equals(targetUserId)) {
            throw new BusinessRuleViolationException("USE_CHANGE_PASSWORD",
                    "Use change-password to change your own password");
        }
        if (!requesterAuthorities.containsAll(targetPermissions)) {
            throw new ForbiddenException("INSUFFICIENT_PRIVILEGE",
                    "You cannot reset the password of a user who holds permissions you do not have");
        }
    }
}
