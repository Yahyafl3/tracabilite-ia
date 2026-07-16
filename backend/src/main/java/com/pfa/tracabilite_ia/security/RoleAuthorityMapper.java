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
            case "USER", "UTILISATEUR" -> "ROLE_USER";
            case "VALIDATOR", "VALIDATEUR" -> "ROLE_VALIDATOR";
            case "AUDITOR", "AUDITEUR" -> "ROLE_AUDITOR";
            default -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
        };
    }
}
