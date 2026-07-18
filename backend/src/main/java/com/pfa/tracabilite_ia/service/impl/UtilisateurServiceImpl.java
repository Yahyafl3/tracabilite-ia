package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.CreerUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.request.ModifierUtilisateurRequest;
import com.pfa.tracabilite_ia.dto.response.UtilisateurResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.exception.UnauthorizedActionException;
import com.pfa.tracabilite_ia.mapper.UtilisateurMapper;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.UtilisateurService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private static final Set<RoleEnum> MANAGED_ROLES = EnumSet.of(
            RoleEnum.ADMINISTRATEUR,
            RoleEnum.VALIDATEUR,
            RoleEnum.AUDITEUR
    );

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurMapper utilisateurMapper;
    private final AuthService authService;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository,
                                  PasswordEncoder passwordEncoder,
                                  UtilisateurMapper utilisateurMapper,
                                  AuthService authService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurMapper = utilisateurMapper;
        this.authService = authService;
    }

    @Override
    public UtilisateurResponse creer(CreerUtilisateurRequest request) {
        assertManagedRole(request.getRole());
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email deja utilise : " + request.getEmail());
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail().trim().toLowerCase());
        utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(request.getRole());
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> lister() {
        return utilisateurMapper.toResponseList(utilisateurRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponse obtenir(UUID id) {
        return utilisateurMapper.toResponse(findUtilisateur(id));
    }

    @Override
    public UtilisateurResponse modifier(UUID id, ModifierUtilisateurRequest request) {
        assertManagedRole(request.getRole());
        Utilisateur utilisateur = findUtilisateur(id);

        if (utilisateur.getRole() == RoleEnum.UTILISATEUR) {
            throw new UnauthorizedActionException(
                    "Seuls les comptes Administrateur, Validateur et Auditeur sont modifiables depuis cette interface.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (utilisateurRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new IllegalArgumentException("Email deja utilise : " + normalizedEmail);
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(normalizedEmail);
        utilisateur.setRole(request.getRole());
        if (request.getMotDePasse() != null && !request.getMotDePasse().isBlank()) {
            utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
        }

        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public void supprimer(UUID id) {
        Utilisateur utilisateur = findUtilisateur(id);
        Utilisateur currentUser = authService.getCurrentUser();

        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedActionException("Impossible de supprimer votre propre compte.");
        }

        if (utilisateur.getRole() == RoleEnum.UTILISATEUR) {
            throw new UnauthorizedActionException(
                    "Seuls les comptes Administrateur, Validateur et Auditeur sont supprimables depuis cette interface.");
        }

        if (utilisateur.getRole() == RoleEnum.ADMINISTRATEUR) {
            long adminCount = utilisateurRepository.findAll().stream()
                    .filter(user -> user.getRole() == RoleEnum.ADMINISTRATEUR)
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Impossible de supprimer le dernier administrateur.");
            }
        }

        utilisateurRepository.delete(utilisateur);
    }

    private Utilisateur findUtilisateur(UUID id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private void assertManagedRole(RoleEnum role) {
        if (role == null || !MANAGED_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "Role non autorise. Roles geres : ADMINISTRATEUR, VALIDATEUR, AUDITEUR.");
        }
    }
}
