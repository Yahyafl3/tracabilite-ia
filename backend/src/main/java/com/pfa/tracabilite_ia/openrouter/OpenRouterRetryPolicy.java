package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import org.springframework.http.HttpHeaders;

import java.util.concurrent.ThreadLocalRandom;

public final class OpenRouterRetryPolicy {

    public static final int MAX_RETRIES = 2;
    private static final long FIRST_DELAY_MS = 2_000L;
    private static final long SECOND_DELAY_MS = 5_000L;

    private OpenRouterRetryPolicy() {
    }

    public static boolean isRetryable(OpenRouterException ex) {
        return switch (ex.getErrorCode()) {
            case OPENROUTER_RATE_LIMITED, OPENROUTER_TIMEOUT, OPENROUTER_UNAVAILABLE,
                 OPENROUTER_MODEL_NOT_FOUND, MODEL_UNAVAILABLE -> true;
            case OPENROUTER_AUTHENTICATION_FAILED, OPENROUTER_CREDIT_ERROR,
                 OPENROUTER_INVALID_RESPONSE -> false;
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
            return null;
        }
        try {
            long seconds = Long.parseLong(value.trim());
            return seconds * 1_000L;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static long jitterMs() {
        return ThreadLocalRandom.current().nextLong(150L, 450L);
    }
}
