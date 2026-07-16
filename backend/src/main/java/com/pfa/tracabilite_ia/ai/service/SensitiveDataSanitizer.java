package com.pfa.tracabilite_ia.ai.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SensitiveDataSanitizer {

    private static final Pattern SENSITIVE_KEY_VALUE = Pattern.compile(
            "(?i)(password|mot[_-]?de[_-]?passe|token|bearer|secret|api[_-]?key|jwt|authorization)\\s*[:=]\\s*\\S+"
    );

    private static final Pattern JWT_PATTERN = Pattern.compile(
            "eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"
    );

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String sanitized = SENSITIVE_KEY_VALUE.matcher(input).replaceAll("$1=[REDACTED]");
        sanitized = JWT_PATTERN.matcher(sanitized).replaceAll("[REDACTED_JWT]");
        return sanitized;
    }
}
