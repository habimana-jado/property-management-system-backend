package rw.afriteck.pms.credit.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;

@Component
public class PhoneNumberNormalizer {

    private final String defaultCountryCode; // e.g. "250", from configuration

    public PhoneNumberNormalizer(@Value("${app.phone.default-country-code}") String defaultCountryCode) {
        this.defaultCountryCode = defaultCountryCode;
    }

    public String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessRuleViolationException("INVALID_PHONE", "Phone number is required");
        }

        String cleaned = raw.replaceAll("[\\s\\-().]", "");

        String normalized;
        if (cleaned.startsWith("+")) {
            normalized = cleaned;
        } else if (cleaned.startsWith("00")) {
            normalized = "+" + cleaned.substring(2);
        } else if (cleaned.startsWith("0")) {
            normalized = "+" + defaultCountryCode + cleaned.substring(1);
        } else if (cleaned.startsWith(defaultCountryCode)) {
            normalized = "+" + cleaned;
        } else {
            normalized = "+" + defaultCountryCode + cleaned;
        }

        if (!normalized.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new BusinessRuleViolationException("INVALID_PHONE", "Phone number is not valid");
        }
        return normalized;
    }
}
