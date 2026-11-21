package rw.afriteck.pms.auth.helpers;

import rw.afriteck.pms.common.exception.BusinessRuleViolationException;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class LoginNames {

    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9][a-z0-9._-]{2,49}$");

    private LoginNames() {}

    /** HOTELCODE-username, used when staff are created. */
    public static String compose(String hotelCode, String username) {
        String code = hotelCode.trim().toUpperCase(Locale.ROOT);
        String name = username.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(name).matches()) {
            throw new BusinessRuleViolationException("INVALID_USERNAME",
                    "Username must be 3-50 characters: letters, digits, dot, dash or underscore");
        }
        return code + "-" + name;
    }

    /** Normalizes what is typed at login. Empty when it is not in HOTELCODE-username form. */
    public static Optional<String> normalizeLogin(String typed) {
        if (typed == null) {
            return Optional.empty();
        }
        String trimmed = typed.trim();
        int dash = trimmed.indexOf('-');
        if (dash <= 0 || dash == trimmed.length() - 1) {
            return Optional.empty();
        }
        return Optional.of(trimmed.substring(0, dash).toUpperCase(Locale.ROOT)
                + "-" + trimmed.substring(dash + 1).toLowerCase(Locale.ROOT));
    }
}
