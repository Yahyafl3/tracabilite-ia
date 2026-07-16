package com.pfa.tracabilite_ia.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRoleConverterTest {

    @Test
    void convertsKeycloakRealmRolesToSpringAuthorities() {
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN", "USER"))
        );

        Collection<GrantedAuthority> authorities = JwtRoleConverter.convertClaims(claims);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void convertsLegacyFrenchRolesFromRoleClaim() {
        Map<String, Object> claims = Map.of("role", "ADMINISTRATEUR");

        Collection<GrantedAuthority> authorities = JwtRoleConverter.convertClaims(claims);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }
}
