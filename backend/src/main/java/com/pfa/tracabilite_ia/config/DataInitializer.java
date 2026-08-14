package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
            AdminDemoAccountSynchronizer adminDemoAccountSynchronizer,
            @Value("${app.demo.seed-enabled:true}") boolean seedEnabled
    ) {
        return args -> {
            updateRoleCheckConstraint(jdbcTemplate);
            updateDecisionStatusCheckConstraint(jdbcTemplate);
            updateDecisionHistoryStatusCheckConstraints(jdbcTemplate);
            ensureUtilisateurActifColumn(jdbcTemplate);

            if (!seedEnabled) {
                log.info(">>> Seed demo desactive (app.demo.seed-enabled=false)");
                return;
            }

            long userCount = utilisateurRepository.count();
            if (userCount == 0) {
                // First bootstrap only — do not recreate accounts after an admin deletes them.
                seedAdmin(utilisateurRepository, passwordEncoder);
                seedUser(utilisateurRepository, passwordEncoder);
                seedAuditeur(utilisateurRepository, passwordEncoder);
                seedDomainValidators(utilisateurRepository, passwordEncoder);
                log.info(">>> Seed initial des utilisateurs de demo termine");
            } else {
                ensureAtLeastOneAdmin(utilisateurRepository, passwordEncoder);
                // Comptes démo manquants (bases Docker déjà peuplées avec d'autres emails).
                seedUser(utilisateurRepository, passwordEncoder);
                seedAuditeur(utilisateurRepository, passwordEncoder);
                seedDomainValidators(utilisateurRepository, passwordEncoder);
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
                CHECK (statut_validation IN (
                    'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 'EN_ATTENTE',
                    'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 'REJETEE', 'ARCHIVEE'
                ))
                """);
    }

    /** Aligne decision_history sur StatutDecisionEnum (agents écrivent ANALYSEE, etc.). */
    private void updateDecisionHistoryStatusCheckConstraints(JdbcTemplate jdbcTemplate) {
        String statuses = """
                'BROUILLON', 'EN_ANALYSE', 'ANALYSEE', 'EN_ATTENTE_VALIDATION', 'EN_ATTENTE',
                'VALIDEE', 'APPROUVEE', 'MODIFIEE', 'A_REVOIR', 'REJETEE', 'ARCHIVEE'
                """;
        jdbcTemplate.execute(
                "ALTER TABLE decision_history DROP CONSTRAINT IF EXISTS decision_history_new_status_check");
        jdbcTemplate.execute(
                "ALTER TABLE decision_history DROP CONSTRAINT IF EXISTS decision_history_previous_status_check");
        jdbcTemplate.execute("""
                ALTER TABLE decision_history ADD CONSTRAINT decision_history_new_status_check
                CHECK (new_status IS NULL OR new_status IN (%s))
                """.formatted(statuses));
        jdbcTemplate.execute("""
                ALTER TABLE decision_history ADD CONSTRAINT decision_history_previous_status_check
                CHECK (previous_status IS NULL OR previous_status IN (%s))
                """.formatted(statuses));
    }

    private void updateRoleCheckConstraint(JdbcTemplate jdbcTemplate) {
        // CRITICAL: Delete any remaining VALIDATEUR accounts before applying the new constraint
        // This ensures the constraint can be applied without violating existing data
        jdbcTemplate.execute("""
                DELETE FROM utilisateur WHERE role = 'VALIDATEUR'
                """);
        
        jdbcTemplate.execute("""
                ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check
                """);
        jdbcTemplate.execute("""
                ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check
                CHECK (role IN (
                    'ADMINISTRATEUR', 'AUDITEUR', 'UTILISATEUR',
                    'AGENT_CREDIT', 'AGENT_SANTE', 'AGENT_PEDAGOGIQUE',
                    'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE'
                ))
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

    /** Validateurs spécialisés par domaine (idempotent sur bases déjà initialisées). */
    private void seedDomainValidators(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        seedIfMissing(utilisateurRepository, passwordEncoder,
                "credit@tracabilite.ia", "Responsable crédit", RoleEnum.RESPONSABLE_CREDIT, "credit123");
        seedIfMissing(utilisateurRepository, passwordEncoder,
                "sante@tracabilite.ia", "Professionnel santé", RoleEnum.PROFESSIONNEL_SANTE, "sante123");
        seedIfMissing(utilisateurRepository, passwordEncoder,
                "pedago@tracabilite.ia", "Responsable pédagogique", RoleEnum.RESPONSABLE_PEDAGOGIQUE, "pedago123");
    }

    private void seedIfMissing(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String nom,
            RoleEnum role,
            String password
    ) {
        if (utilisateurRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasseHash(passwordEncoder.encode(password));
        u.setRole(role);
        utilisateurRepository.save(u);
        log.info(">>> Validateur domaine cree : {} / {} ({})", email, password, role);
    }
}
