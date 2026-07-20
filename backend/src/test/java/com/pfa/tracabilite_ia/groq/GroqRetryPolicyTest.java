package com.pfa.tracabilite_ia.groq;

import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class GroqRetryPolicyTest {

    @Test
    void rateLimited_isRetryable() {
        GroqException ex = new GroqException(GroqErrorCode.GROQ_RATE_LIMITED, "429", 429, null, null);
        assertThat(GroqRetryPolicy.isRetryable(ex)).isTrue();
    }

    @Test
    void authFailed_isNotRetryable() {
        GroqException ex = new GroqException(GroqErrorCode.GROQ_AUTHENTICATION_FAILED, "401", 401, null, null);
        assertThat(GroqRetryPolicy.isRetryable(ex)).isFalse();
    }

    @Test
    void delay_respectsRetryAfterHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, "3");
        long delay = GroqRetryPolicy.delayBeforeRetry(0, headers);
        assertThat(delay).isGreaterThanOrEqualTo(3000L);
        assertThat(delay).isLessThan(4000L);
    }

    @Test
    void delay_usesBackoffWhenNoRetryAfter() {
        long first = GroqRetryPolicy.delayBeforeRetry(0, null);
        long second = GroqRetryPolicy.delayBeforeRetry(1, null);
        assertThat(first).isBetween(2000L, 2600L);
        assertThat(second).isBetween(5000L, 5600L);
    }
}
