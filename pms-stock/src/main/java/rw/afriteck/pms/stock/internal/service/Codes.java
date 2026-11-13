package rw.afriteck.pms.stock.internal.service;

import rw.afriteck.pms.common.exception.StockRuleViolationException;

import java.util.Locale;
import java.util.regex.Pattern;

final class Codes {
    private static final Pattern VALID = Pattern.compile("[A-Z0-9][A-Z0-9._-]{0,49}");

    static String normalize(String raw) {
        String code = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (!VALID.matcher(code).matches()) {
            throw new StockRuleViolationException(
                    "Invalid code '" + raw + "': use letters, digits, '.', '_' or '-' (max 50 characters)");
        }
        return code;
    }
}

