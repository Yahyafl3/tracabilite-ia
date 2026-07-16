package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Utilisateur;

public interface AuthService {
    Utilisateur login(String email, String motDePasse);
    Utilisateur getCurrentUser();
    java.util.Collection<org.springframework.security.core.GrantedAuthority> getCurrentAuthorities();
    void logout();
}
