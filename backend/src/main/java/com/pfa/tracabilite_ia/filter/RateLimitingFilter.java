package com.pfa.tracabilite_ia.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting in-memory (fenêtre glissante) pour login, reset password et exports.
 * Pour un cluster multi-instances, migrer vers Redis / Bucket4j distribué.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Deque<Instant>> windows = new ConcurrentHashMap<>();

    private final int loginCapacity;
    private final int passwordCapacity;
    private final int exportCapacity;

    public RateLimitingFilter(
            @Value("${app.rate-limit.login.capacity:20}") int loginCapacity,
            @Value("${app.rate-limit.password.capacity:10}") int passwordCapacity,
            @Value("${app.rate-limit.export.capacity:30}") int exportCapacity
    ) {
        this.loginCapacity = loginCapacity;
        this.passwordCapacity = passwordCapacity;
        this.exportCapacity = exportCapacity;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        LimitSpec spec = resolve(path, method);
        if (spec == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = spec.name() + ":" + clientKey(request);
        if (!tryAcquire(key, spec.capacity())) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"status\":429,\"code\":\"RATE_LIMITED\",\"message\":\"Trop de requêtes. Réessayez plus tard.\"}"
            );
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean tryAcquire(String key, int capacity) {
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(60);
        Deque<Instant> q = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst().isBefore(cutoff)) {
                q.removeFirst();
            }
            if (q.size() >= capacity) {
                return false;
            }
            q.addLast(now);
            return true;
        }
    }

    private LimitSpec resolve(String path, String method) {
        if ("POST".equalsIgnoreCase(method) && path.endsWith("/api/auth/login")) {
            return new LimitSpec("login", loginCapacity);
        }
        if ("POST".equalsIgnoreCase(method) && (path.contains("/api/auth/forgot-password")
                || path.contains("/api/auth/reset-password"))) {
            return new LimitSpec("password", passwordCapacity);
        }
        if ("GET".equalsIgnoreCase(method) && path.contains("/api/decisions/export")) {
            return new LimitSpec("export", exportCapacity);
        }
        return null;
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private record LimitSpec(String name, int capacity) {}
}
