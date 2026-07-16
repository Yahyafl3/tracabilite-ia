package com.pfa.tracabilite_ia.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Convertisseur de roles Keycloak (realm_access.roles) ou JWT maison (claim role)
 * vers les authorities Spring. Compatible avec oauth2ResourceServer via adaptation
 * des claims sans dependance oauth2 au build.
 */
public final class JwtRoleConverter {

    private JwtRoleConverter() {
    }

    @SuppressWarnings("unchecked")
    public static Collection<GrantedAuthority> convertClaims(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return List.of();
        }

        Object realmAccessObject = claims.get("realm_access");
        if (realmAccessObject instanceof Map<?, ?> realmAccess) {
            Collection<GrantedAuthority> keycloakAuthorities =
                    RoleAuthorityMapper.fromKeycloakJwt((Map<String, Object>) realmAccess);
            if (!keycloakAuthorities.isEmpty()) {
                return keycloakAuthorities;
            }
        }

        Object roleObject = claims.get("role");
        if (roleObject != null) {
            return RoleAuthorityMapper.fromRoleClaim(roleObject.toString());
        }

        return List.of();
    }
}
