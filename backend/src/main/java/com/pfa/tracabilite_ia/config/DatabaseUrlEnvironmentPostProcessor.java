package com.pfa.tracabilite_ia.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps cloud-friendly DATABASE_* variables (Neon / Render) onto Spring datasource properties.
 * Supports {@code postgres://}, {@code postgresql://} and {@code jdbc:postgresql://} URLs.
 * Does not log credentials.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> overrides = new LinkedHashMap<>();

        String databaseUrl = trimToNull(environment.getProperty("DATABASE_URL"));
        if (databaseUrl != null) {
            ParsedJdbc parsed = parseDatabaseUrl(databaseUrl);
            overrides.put("spring.datasource.url", parsed.jdbcUrl());
            if (trimToNull(environment.getProperty("DATABASE_USERNAME")) == null
                    && trimToNull(environment.getProperty("SPRING_DATASOURCE_USERNAME")) == null
                    && parsed.username() != null) {
                overrides.put("spring.datasource.username", parsed.username());
            }
            if (environment.getProperty("DATABASE_PASSWORD") == null
                    && environment.getProperty("SPRING_DATASOURCE_PASSWORD") == null
                    && parsed.password() != null) {
                overrides.put("spring.datasource.password", parsed.password());
            }
        }

        String username = trimToNull(environment.getProperty("DATABASE_USERNAME"));
        if (username != null) {
            overrides.put("spring.datasource.username", username);
        }
        if (environment.getProperty("DATABASE_PASSWORD") != null) {
            overrides.put("spring.datasource.password", environment.getProperty("DATABASE_PASSWORD"));
        }

        if (!overrides.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("cloudDatabaseMapping", overrides));
        }
    }

    static ParsedJdbc parseDatabaseUrl(String raw) {
        String value = raw.trim();
        if (value.startsWith("jdbc:postgresql://")) {
            return new ParsedJdbc(ensureSslMode(value), null, null);
        }

        String normalized = value;
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        }
        if (!normalized.startsWith("postgresql://")) {
            throw new IllegalArgumentException(
                    "DATABASE_URL must be a postgres/postgresql or jdbc:postgresql URL");
        }

        URI uri = URI.create(normalized);
        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null && !userInfo.isBlank()) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = decode(userInfo.substring(0, colon));
                password = decode(userInfo.substring(colon + 1));
            } else {
                username = decode(userInfo);
            }
        }

        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/postgres" : uri.getPath();
        String query = uri.getRawQuery();
        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(':')
                .append(port)
                .append(path);
        if (query != null && !query.isBlank()) {
            jdbc.append('?').append(query);
        }
        return new ParsedJdbc(ensureSslMode(jdbc.toString()), username, password);
    }

    private static String ensureSslMode(String jdbcUrl) {
        String lower = jdbcUrl.toLowerCase();
        boolean neonHost = lower.contains("neon.tech");
        boolean hasSsl = lower.contains("sslmode=");
        if (!neonHost || hasSsl) {
            return jdbcUrl;
        }
        return jdbcUrl.contains("?") ? jdbcUrl + "&sslmode=require" : jdbcUrl + "?sslmode=require";
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    record ParsedJdbc(String jdbcUrl, String username, String password) {
    }
}
