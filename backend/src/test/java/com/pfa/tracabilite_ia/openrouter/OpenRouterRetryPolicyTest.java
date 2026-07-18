package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterRetryPolicyTest {

    @Test
    void isRetryable_allowsTemporaryErrors() {
        assertThat(OpenRouterRetryPolicy.isRetryable(
                new OpenRouterException(OpenRouterErrorCode.OPENROUTER_RATE_LIMITED, "rate"))).isTrue();
        assertThat(OpenRouterRetryPolicy.isRetryable(
                new OpenRouterException(OpenRouterErrorCode.OPENROUTER_TIMEOUT, "timeout"))).isTrue();
        assertThat(OpenRouterRetryPolicy.isRetryable(
                new OpenRouterException(OpenRouterErrorCode.OPENROUTER_UNAVAILABLE, "503"))).isTrue();
    }

    @Test
    void isRetryable_rejectsAuthenticationErrors() {
        assertThat(OpenRouterRetryPolicy.isRetryable(
                new OpenRouterException(OpenRouterErrorCode.OPENROUTER_AUTHENTICATION_FAILED, "401"))).isFalse();
    }

    @Test
    void delayBeforeRetry_usesRetryAfterHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, "3");

        long delay = OpenRouterRetryPolicy.delayBeforeRetry(0, headers);

        assertThat(delay).isGreaterThanOrEqualTo(3_000L);
    }

    @Test
    void delayBeforeRetry_usesConfiguredBackoff() {
        long first = OpenRouterRetryPolicy.delayBeforeRetry(0, null);
        long second = OpenRouterRetryPolicy.delayBeforeRetry(1, null);

        assertThat(first).isBetween(2_000L, 2_500L);
        assertThat(second).isBetween(5_000L, 5_500L);
    }
}
