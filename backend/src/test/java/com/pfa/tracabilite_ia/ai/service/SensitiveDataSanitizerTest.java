package com.pfa.tracabilite_ia.ai.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataSanitizerTest {

    private final SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();

    @Test
    void redactsPasswordAndTokenFields() {
        String input = "email=test@mail.com password=secret123 token=abc bearer xyz";

        String sanitized = sanitizer.sanitize(input);

        assertThat(sanitized).doesNotContain("secret123");
        assertThat(sanitized).contains("password=[REDACTED]");
        assertThat(sanitized).contains("token=[REDACTED]");
    }

    @Test
    void redactsJwtPattern() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3jVmNHl0w5N_XgL0n3I9PlFUP0THsR8U";
        String sanitized = sanitizer.sanitize("Bearer " + jwt);

        assertThat(sanitized).contains("[REDACTED_JWT]");
        assertThat(sanitized).doesNotContain(jwt);
    }
}
