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
            RoleEnum.AGENT_CREDIT,
            RoleEnum.AGENT_SANTE,
            RoleEnum.AGENT_PEDAGOGIQUE,
            RoleEnum.RESPONSABLE_CREDIT,
            RoleEnum.PROFESSIONNEL_SANTE,
            RoleEnum.RESPONSABLE_PEDAGOGIQUE,
            RoleEnum.AUDITEUR,
            RoleEnum.ADMINISTRATEUR,
            // Legacy — kept for existing accounts; backend still accepts them on update
            RoleEnum.UTILISATEUR
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
        utilisateur.setActif(true);
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

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (utilisateurRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new IllegalArgumentException("Email deja utilise : " + normalizedEmail);
        }

        if (utilisateur.getRole() == RoleEnum.ADMINISTRATEUR
                && request.getRole() != RoleEnum.ADMINISTRATEUR
                && isLastActiveAdmin(utilisateur.getId())) {
            throw new IllegalStateException("Impossible de retirer le rôle du dernier administrateur actif.");
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
    public UtilisateurResponse desactiver(UUID id) {
        Utilisateur utilisateur = findUtilisateur(id);
        Utilisateur currentUser = authService.getCurrentUser();

        if (currentUser.getId().equals(id)) {
            throw new UnauthorizedActionException("Impossible de désactiver votre propre compte.");
        }

        if (utilisateur.getRole() == RoleEnum.ADMINISTRATEUR && isLastActiveAdmin(id)) {
            throw new IllegalStateException("Impossible de désactiver le dernier administrateur actif.");
        }

        utilisateur.setActif(false);
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    public UtilisateurResponse reactiver(UUID id) {
        Utilisateur utilisateur = findUtilisateur(id);
        utilisateur.setActif(true);
        return utilisateurMapper.toResponse(utilisateurRepository.save(utilisateur));
    }

    private boolean isLastActiveAdmin(UUID candidateId) {
        return utilisateurRepository.findAll().stream()
                .filter(user -> user.getRole() == RoleEnum.ADMINISTRATEUR)
                .filter(Utilisateur::isActif)
                .noneMatch(user -> !user.getId().equals(candidateId));
    }

    private Utilisateur findUtilisateur(UUID id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private void assertManagedRole(RoleEnum role) {
        if (role == null || !MANAGED_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "Role non autorise. Roles geres : AGENT_CREDIT, AGENT_SANTE, AGENT_PEDAGOGIQUE, "
                            + "RESPONSABLE_CREDIT, PROFESSIONNEL_SANTE, RESPONSABLE_PEDAGOGIQUE, "
                            + "AUDITEUR, ADMINISTRATEUR.");
        }
    }
}
