package com.pfa.tracabilite_ia.groq;

import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import org.springframework.http.HttpHeaders;

import java.util.concurrent.ThreadLocalRandom;

public final class GroqRetryPolicy {

    public static final int MAX_RETRIES = 2;
    private static final long FIRST_DELAY_MS = 2_000L;
    private static final long SECOND_DELAY_MS = 5_000L;

    private GroqRetryPolicy() {
    }

    public static boolean isRetryable(GroqException ex) {
        return switch (ex.getErrorCode()) {
            case GROQ_RATE_LIMITED, GROQ_TIMEOUT, GROQ_UNAVAILABLE,
                 GROQ_MODEL_NOT_FOUND, MODEL_UNAVAILABLE -> true;
            case GROQ_AUTHENTICATION_FAILED, GROQ_INVALID_RESPONSE -> false;
        };
    }

    public static long delayBeforeRetry(int retryIndex, HttpHeaders responseHeaders) {
        Long retryAfterMs = parseRetryAfterMs(responseHeaders);
        if (retryAfterMs != null && retryAfterMs > 0) {
            return retryAfterMs + jitterMs();
        }
        long base = retryIndex == 0 ? FIRST_DELAY_MS : SECOND_DELAY_MS;
        return base + jitterMs();
    }

    private static Long parseRetryAfterMs(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String value = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null || value.isBlank()) {
            value = headers.getFirst("retry-after");
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim()) * 1_000L;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static long jitterMs() {
        return ThreadLocalRandom.current().nextLong(150L, 450L);
    }
}
