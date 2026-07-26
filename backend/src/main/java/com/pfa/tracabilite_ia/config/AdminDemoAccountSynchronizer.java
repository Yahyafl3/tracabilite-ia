package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Met à jour l'email du compte administrateur de démonstration sur les bases déjà initialisées (Neon).
 * Ne crée pas de second admin et ne modifie ni mot de passe, ni rôle, ni nom.
 */
@Component
public class AdminDemoAccountSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(AdminDemoAccountSynchronizer.class);

    /** Ancien email seed (documentation / bases existantes). */
    public static final String LEGACY_ADMIN_EMAIL = "admin@tracabilite.ia";

    /** Email admin de démonstration (Resend / mot de passe oublié). */
    public static final String TARGET_ADMIN_EMAIL = "0629378510a@gmail.com";

    private final UtilisateurRepository utilisateurRepository;

    public AdminDemoAccountSynchronizer(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public void syncAdminEmail() {
        String target = normalizeEmail(TARGET_ADMIN_EMAIL);
        String legacy = normalizeEmail(LEGACY_ADMIN_EMAIL);

        Optional<Utilisateur> targetOwner = utilisateurRepository.findByEmailIgnoreCase(target);
        if (targetOwner.isPresent()) {
            Utilisateur owner = targetOwner.get();
            if (owner.getRole() == RoleEnum.ADMINISTRATEUR) {
                log.info(">>> Compte admin deja a jour (email cible present)");
                return;
            }
            log.warn(">>> Email deja utilise — mise a jour admin ignoree");
            return;
        }

        Optional<Utilisateur> byLegacy = utilisateurRepository.findByEmailIgnoreCase(legacy);
        Utilisateur admin = null;
        if (byLegacy.isPresent() && byLegacy.get().getRole() == RoleEnum.ADMINISTRATEUR) {
            admin = byLegacy.get();
        } else {
            admin = utilisateurRepository.findAll().stream()
                    .filter(u -> u.getRole() == RoleEnum.ADMINISTRATEUR)
                    .filter(u -> legacy.equalsIgnoreCase(normalizeEmail(u.getEmail())))
                    .findFirst()
                    .orElse(null);
        }

        if (admin == null) {
            boolean hasAdmin = utilisateurRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == RoleEnum.ADMINISTRATEUR);
            if (!hasAdmin) {
                log.warn(">>> Compte admin introuvable — aucune mise a jour d'email");
            } else {
                log.info(">>> Compte admin present sans email legacy — aucune migration necessaire");
            }
            return;
        }

        if (utilisateurRepository.existsByEmailIgnoreCaseAndIdNot(target, admin.getId())) {
            log.warn(">>> Email deja utilise — mise a jour admin ignoree");
            return;
        }

        admin.setEmail(target);
        utilisateurRepository.save(admin);
        log.info(">>> Compte admin mis a jour");
    }

    static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
