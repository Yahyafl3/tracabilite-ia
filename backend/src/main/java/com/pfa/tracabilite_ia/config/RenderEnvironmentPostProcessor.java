package com.pfa.tracabilite_ia.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Render sets {@code RENDER=true} and {@code PORT}. Activates {@code prod} when no profile
 * is configured and applies startup tuning for free-tier instances (slow cold start).
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RenderEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE = "renderCloudDefaults";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!"true".equalsIgnoreCase(trimToNull(environment.getProperty("RENDER")))) {
            return;
        }

        Map<String, Object> overrides = new LinkedHashMap<>();

        if (environment.getActiveProfiles().length == 0
                && trimToNull(environment.getProperty("SPRING_PROFILES_ACTIVE")) == null) {
            application.setAdditionalProfiles("prod");
        }

        overrides.put("spring.main.lazy-initialization", "true");
        overrides.put("spring.data.jpa.repositories.bootstrap-mode", "deferred");
        overrides.put("spring.jmx.enabled", "false");

        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE, overrides));
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
