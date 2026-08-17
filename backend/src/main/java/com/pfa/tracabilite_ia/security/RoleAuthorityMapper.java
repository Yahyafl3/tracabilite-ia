package com.pfa.tracabilite_ia.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class RoleAuthorityMapper {

    private RoleAuthorityMapper() {
    }

    public static Collection<GrantedAuthority> fromRoleClaim(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        String upper = role.toUpperCase();
        if (upper.equals("RESPONSABLE_CREDIT")
                || upper.equals("PROFESSIONNEL_SANTE")
                || upper.equals("RESPONSABLE_PEDAGOGIQUE")) {
            return domainAuthorities(upper);
        }
        // Agent roles get domain-scoped USER authority so the security layer knows their domain
        if (upper.equals("AGENT_CREDIT")
                || upper.equals("AGENT_SANTE")
                || upper.equals("AGENT_PEDAGOGIQUE")) {
            return agentAuthorities(upper);
        }
        return List.of(new SimpleGrantedAuthority(mapToSpringRole(role)));
    }

    @SuppressWarnings("unchecked")
    public static Collection<GrantedAuthority> fromKeycloakJwt(Map<String, Object> realmAccess) {
        if (realmAccess == null) {
            return List.of();
        }

        Object rolesObject = realmAccess.get("roles");
        if (!(rolesObject instanceof List<?> roles)) {
            return List.of();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Object roleObject : roles) {
            if (roleObject != null) {
                authorities.add(new SimpleGrantedAuthority(mapToSpringRole(roleObject.toString())));
            }
        }
        return authorities;
    }

    public static String mapToSpringRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN", "ADMINISTRATEUR" -> "ROLE_ADMIN";
            // UTILISATEUR/VALIDATEUR ne sont acceptés que le temps que les JWT émis avant la
            // migration des rôles expirent ; la portée réelle vient du rôle relu en base.
            case "USER", "UTILISATEUR",
                 "AGENT_CREDIT", "AGENT_SANTE", "AGENT_PEDAGOGIQUE" -> "ROLE_USER";
            case "VALIDATOR", "VALIDATEUR",
                 "RESPONSABLE_CREDIT", "PROFESSIONNEL_SANTE", "RESPONSABLE_PEDAGOGIQUE"
                    -> "ROLE_VALIDATOR";
            case "AUDITOR", "AUDITEUR" -> "ROLE_AUDITOR";
            default -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        };
    }

    /**
     * Autorités supplémentaires spécifiques au domaine (en plus de ROLE_VALIDATOR).
     */
    public static Collection<GrantedAuthority> domainAuthorities(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return switch (role.toUpperCase()) {
            case "RESPONSABLE_CREDIT" -> List.of(
                    new SimpleGrantedAuthority("ROLE_VALIDATOR"),
                    new SimpleGrantedAuthority("ROLE_CREDIT_VALIDATOR")
            );
            case "PROFESSIONNEL_SANTE" -> List.of(
                    new SimpleGrantedAuthority("ROLE_VALIDATOR"),
                    new SimpleGrantedAuthority("ROLE_MEDICAL_VALIDATOR")
            );
            case "RESPONSABLE_PEDAGOGIQUE" -> List.of(
                    new SimpleGrantedAuthority("ROLE_VALIDATOR"),
                    new SimpleGrantedAuthority("ROLE_EDUCATION_VALIDATOR")
            );
            default -> fromRoleClaim(role);
        };
    }

    /**
     * Autorités pour les agents créateurs par domaine (ROLE_USER + domaine spécifique).
     */
    public static Collection<GrantedAuthority> agentAuthorities(String role) {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return switch (role.toUpperCase()) {
            case "AGENT_CREDIT" -> List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_AGENT_CREDIT")
            );
            case "AGENT_SANTE" -> List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_AGENT_SANTE")
            );
            case "AGENT_PEDAGOGIQUE" -> List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_AGENT_PEDAGOGIQUE")
            );
            default -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
        };
    }
}
