package com.pfa.tracabilite_ia.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fail-fast en profil {@code prod} si secrets / DB absents ou trop faibles.
 * Enregistré aussi via {@link META-INF/spring.factories} n'est pas requis :
 * le bean démarre après le contexte ; pour un échec avant JPA, voir aussi
 * {@link ProductionEnvironmentValidator.EarlyProdGuard}.
 */
@Component
public class ProductionEnvironmentValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionEnvironmentValidator.class);
    private static final int MIN_JWT_SECRET_LENGTH = 32;
    private static final int MIN_ML_TOKEN_LENGTH = 24;
    private static final List<String> FORBIDDEN_JWT = List.of(
            "change-me",
            "tracabilite-ia-super-secret-key-please-change-in-production",
            "secret"
    );
    private static final List<String> FORBIDDEN_DB_PASSWORDS = List.of(
            "tracabilite123",
            "change-me-local-only",
            "password",
            "postgres"
    );
    private static final List<String> FORBIDDEN_ML_TOKENS = List.of(
            "change-me",
            "local-dev-only",
            "secret",
            "token"
    );

    public ProductionEnvironmentValidator(
            Environment environment,
            @Value("${jwt.secret:}") String jwtSecret,
            @Value("${spring.datasource.password:}") String dbPassword,
            @Value("${spring.datasource.url:}") String dbUrl,
            @Value("${spring.datasource.username:}") String dbUsername,
            @Value("${ml.service.token:}") String mlServiceToken
    ) {
        if (!isProd(environment)) {
            return;
        }
        List<String> errors = new ArrayList<>();

        if (jwtSecret == null || jwtSecret.isBlank()) {
            errors.add("JWT_SECRET est obligatoire en production");
        } else if (jwtSecret.length() < MIN_JWT_SECRET_LENGTH) {
            errors.add("JWT_SECRET doit contenir au moins " + MIN_JWT_SECRET_LENGTH + " caractères");
        } else if (FORBIDDEN_JWT.stream().anyMatch(f -> jwtSecret.toLowerCase().contains(f.toLowerCase()))) {
            errors.add("JWT_SECRET utilise une valeur interdite / trop faible");
        }

        if (dbUrl == null || dbUrl.isBlank()) {
            errors.add("SPRING_DATASOURCE_URL / DATABASE_URL est obligatoire en production");
        }
        if (dbUsername == null || dbUsername.isBlank()) {
            errors.add("Identifiant DB obligatoire en production");
        }
        if (dbPassword == null || dbPassword.isBlank()) {
            errors.add("Mot de passe DB obligatoire en production");
        } else if (FORBIDDEN_DB_PASSWORDS.stream().anyMatch(p -> p.equalsIgnoreCase(dbPassword))) {
            errors.add("Mot de passe DB trop faible / valeur de démonstration interdite en production");
        }

        if (mlServiceToken == null || mlServiceToken.isBlank()) {
            errors.add("ML_SERVICE_TOKEN est obligatoire en production");
        } else if (mlServiceToken.length() < MIN_ML_TOKEN_LENGTH) {
            errors.add("ML_SERVICE_TOKEN doit contenir au moins " + MIN_ML_TOKEN_LENGTH + " caractères");
        } else if (FORBIDDEN_ML_TOKENS.stream().anyMatch(f -> mlServiceToken.toLowerCase().contains(f))) {
            errors.add("ML_SERVICE_TOKEN utilise une valeur interdite / trop faible");
        }

        String ddl = environment.getProperty("spring.jpa.hibernate.ddl-auto", "");
        if (!"validate".equalsIgnoreCase(ddl) && !"none".equalsIgnoreCase(ddl)) {
            errors.add("spring.jpa.hibernate.ddl-auto doit être validate ou none en production (actuel=" + ddl + ")");
        }

        if (!errors.isEmpty()) {
            errors.forEach(e -> log.error("[PROD] {}", e));
            throw new IllegalStateException(
                    "Configuration production invalide (" + errors.size() + " erreur(s)). Voir logs."
            );
        }
        log.info("Validation environnement production OK (secrets présents, ddl-auto conforme)");
    }

    private static boolean isProd(Environment environment) {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
    }

    /**
     * Garde très tôt (avant création du contexte) pour les profils prod.
     */
    public static class EarlyProdGuard implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
        @Override
        public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
            Environment env = event.getEnvironment();
            if (!isProd(env)) {
                return;
            }
            String jwt = env.getProperty("JWT_SECRET", env.getProperty("jwt.secret", ""));
            if (jwt == null || jwt.isBlank() || jwt.length() < MIN_JWT_SECRET_LENGTH) {
                throw new IllegalStateException("JWT_SECRET manquant ou trop court pour le profil prod");
            }
        }
    }
}
