package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedDemoData(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate,
            AdminDemoAccountSynchronizer adminDemoAccountSynchronizer
    ) {
        return args -> {
            updateRoleCheckConstraint(jdbcTemplate);
            updateDecisionStatusCheckConstraint(jdbcTemplate);
            ensureUtilisateurActifColumn(jdbcTemplate);

            long userCount = utilisateurRepository.count();
            if (userCount == 0) {
                // First bootstrap only — do not recreate accounts after an admin deletes them.
                seedAdmin(utilisateurRepository, passwordEncoder);
                seedUser(utilisateurRepository, passwordEncoder);
                seedValidateur(utilisateurRepository, passwordEncoder);
                seedAuditeur(utilisateurRepository, passwordEncoder);
                log.info(">>> Seed initial des utilisateurs de demo termine");
            } else {
                ensureAtLeastOneAdmin(utilisateurRepository, passwordEncoder);
                log.info(">>> Seed utilisateurs ignore (base deja initialisee, {} compte(s))", userCount);
            }

            // Bases Neon / Postgres deja initialisees : migrer l'email admin sans recreer le compte.
            adminDemoAccountSynchronizer.syncAdminEmail();
        };
    }

    private void ensureAtLeastOneAdmin(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder
    ) {
        boolean hasAdmin = utilisateurRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == RoleEnum.ADMINISTRATEUR);
        if (!hasAdmin) {
            log.warn(">>> Aucun administrateur trouve — recreation du compte admin de secours");
            seedAdmin(utilisateurRepository, passwordEncoder);
        }
    }

    private void updateDecisionStatusCheckConstraint(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                ALTER TABLE decision DROP CONSTRAINT IF EXISTS decision_statut_validation_check
                """);
        jdbcTemplate.execute("""
                ALTER TABLE decision ADD CONSTRAINT decision_statut_validation_check
                CHECK (statut_validation IN ('BROUILLON', 'EN_ATTENTE', 'APPROUVEE', 'MODIFIEE', 'REJETEE'))
                """);
    }

    private void updateRoleCheckConstraint(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check
                """);
        jdbcTemplate.execute("""
                ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
                CHECK (role IN ('ADMINISTRATEUR', 'VALIDATEUR', 'AUDITEUR', 'UTILISATEUR'))
                """);
    }

    /** Soft-disable flag — default true for existing rows. */
    private void ensureUtilisateurActifColumn(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                ALTER TABLE utilisateur
                ADD COLUMN IF NOT EXISTS actif BOOLEAN NOT NULL DEFAULT TRUE
                """);
        jdbcTemplate.execute("""
                UPDATE utilisateur SET actif = TRUE WHERE actif IS NULL
                """);
    }

    private void seedAdmin(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        String email = AdminDemoAccountSynchronizer.normalizeEmail(
                AdminDemoAccountSynchronizer.TARGET_ADMIN_EMAIL
        );
        if (!utilisateurRepository.existsByEmailIgnoreCase(email)
                && !utilisateurRepository.existsByEmailIgnoreCase(AdminDemoAccountSynchronizer.LEGACY_ADMIN_EMAIL)) {
            Utilisateur admin = new Utilisateur();
            admin.setNom("Administrateur");
            admin.setEmail(email);
            admin.setMotDePasseHash(passwordEncoder.encode("admin123"));
            admin.setRole(RoleEnum.ADMINISTRATEUR);
            utilisateurRepository.save(admin);
            log.info(">>> Utilisateur admin cree (mot de passe demo conservé)");
        }
    }

    private void seedUser(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        String email = "user@tracabilite.ia";
        if (!utilisateurRepository.existsByEmailIgnoreCase(email)) {
            Utilisateur user = new Utilisateur();
            user.setNom("Agent de crédit");
            user.setEmail(email);
            user.setMotDePasseHash(passwordEncoder.encode("user123"));
            user.setRole(RoleEnum.UTILISATEUR);
            utilisateurRepository.save(user);
            log.info(">>> Agent de credit (UTILISATEUR) cree : {} / user123", email);
        }
    }

    private void seedAuditeur(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        String email = "auditeur@tracabilite.ia";
        if (!utilisateurRepository.existsByEmailIgnoreCase(email)) {
            // Compat: ancien seed utilisait auditor@...
            if (utilisateurRepository.existsByEmailIgnoreCase("auditor@tracabilite.ia")) {
                return;
            }
            Utilisateur auditor = new Utilisateur();
            auditor.setNom("Auditeur");
            auditor.setEmail(email);
            auditor.setMotDePasseHash(passwordEncoder.encode("auditor123"));
            auditor.setRole(RoleEnum.AUDITEUR);
            utilisateurRepository.save(auditor);
            log.info(">>> Utilisateur auditeur cree : {} / auditor123", email);
        }
    }

    private void seedValidateur(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        String email = "validateur@tracabilite.ia";
        if (!utilisateurRepository.existsByEmailIgnoreCase(email)) {
            Utilisateur validateur = new Utilisateur();
            validateur.setNom("Validateur");
            validateur.setEmail(email);
            validateur.setMotDePasseHash(passwordEncoder.encode("validateur123"));
            validateur.setRole(RoleEnum.VALIDATEUR);
            utilisateurRepository.save(validateur);
            log.info(">>> Utilisateur validateur cree : {} / validateur123", email);
        }
    }
}
