package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.UtilisateurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Utilisateur creer(CreerUtilisateurRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email deja utilise");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }
}
