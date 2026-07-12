package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Utilisateur login(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasseHash())) {
            throw new RuntimeException("Mot de passe incorrect");
        }
        return utilisateur;
    }

    @Override
    public Utilisateur getCurrentUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
