package com.pfa.tracabilite_ia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory cooldown to limit repeated support submissions by email or IP.
 */
@Component
public class SupportRateLimiter {

    private final long cooldownSeconds;
    private final Map<String, Instant> lastSubmission = new ConcurrentHashMap<>();

    public SupportRateLimiter(
            @Value("${app.support.rate-limit-seconds:120}") long cooldownSeconds
    ) {
        this.cooldownSeconds = Math.max(30, cooldownSeconds);
    }

    public void checkAllowed(String email, String clientIp) {
        Instant now = Instant.now();
        purgeExpired(now);

        String emailKey = "email:" + normalize(email);
        String ipKey = "ip:" + normalize(clientIp == null || clientIp.isBlank() ? "unknown" : clientIp);

        if (isCoolingDown(emailKey, now) || isCoolingDown(ipKey, now)) {
            throw new IllegalArgumentException(
                    "Trop de demandes récentes. Veuillez réessayer dans quelques minutes."
            );
        }

        lastSubmission.put(emailKey, now);
        lastSubmission.put(ipKey, now);
    }

    private boolean isCoolingDown(String key, Instant now) {
        Instant previous = lastSubmission.get(key);
        return previous != null && previous.plusSeconds(cooldownSeconds).isAfter(now);
    }

    private void purgeExpired(Instant now) {
        lastSubmission.entrySet().removeIf(entry ->
                entry.getValue().plusSeconds(cooldownSeconds * 2).isBefore(now));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
