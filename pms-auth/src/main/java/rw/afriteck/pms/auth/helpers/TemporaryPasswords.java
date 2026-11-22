package rw.afriteck.pms.auth.helpers;

import java.security.SecureRandom;

public final class TemporaryPasswords {

    // no I, O, l, 0 or 1: easy to read out loud and to type
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LENGTH = 12;

    private TemporaryPasswords() {}

    public static String generate() {
        while (true) {
            StringBuilder sb = new StringBuilder(LENGTH);
            for (int i = 0; i < LENGTH; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            String candidate = sb.toString();
            if (candidate.chars().anyMatch(Character::isLetter) && candidate.chars().anyMatch(Character::isDigit)) {
                return candidate;
            }
        }
    }
}
