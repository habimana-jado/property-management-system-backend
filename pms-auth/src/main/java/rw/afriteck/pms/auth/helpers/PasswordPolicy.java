package rw.afriteck.pms.auth.helpers;

import rw.afriteck.pms.common.exception.BusinessRuleViolationException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_BYTES = 72;   // BCrypt only reads the first 72 bytes

    private PasswordPolicy() {}

    public static void validate(String password, String loginName) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw weak("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw weak("Password is too long");
        }
        if (password.chars().noneMatch(Character::isLetter) || password.chars().noneMatch(Character::isDigit)) {
            throw weak("Password must contain at least one letter and one digit");
        }
        String name = loginName.substring(loginName.indexOf('-') + 1).toLowerCase(Locale.ROOT);
        if (name.length() >= 3 && password.toLowerCase(Locale.ROOT).contains(name)) {
            throw weak("Password must not contain your username");
        }
    }

    public static void validate(String password, String loginName, int minLength) {
        if (password == null || password.length() < minLength) {
            throw weak("Password must be at least " + minLength + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw weak("Password is too long");
        }
        if (password.chars().noneMatch(Character::isLetter) || password.chars().noneMatch(Character::isDigit)) {
            throw weak("Password must contain at least one letter and one digit");
        }
        String name = loginName.substring(loginName.indexOf('-') + 1).toLowerCase(Locale.ROOT);
        if (name.length() >= 3 && password.toLowerCase(Locale.ROOT).contains(name)) {
            throw weak("Password must not contain your username");
        }
    }

    private static BusinessRuleViolationException weak(String message) {
        return new BusinessRuleViolationException("WEAK_PASSWORD", message);
    }
}
