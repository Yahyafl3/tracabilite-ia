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
    public CommandLineRunner seedDemoData(UtilisateurRepository utilisateurRepository,
                                          PasswordEncoder passwordEncoder,
                                          JdbcTemplate jdbcTemplate) {
        return args -> {
            updateRoleCheckConstraint(jdbcTemplate);
            updateDecisionStatusCheckConstraint(jdbcTemplate);
            seedAdmin(utilisateurRepository, passwordEncoder);
            seedUser(utilisateurRepository, passwordEncoder);
            seedValidateur(utilisateurRepository, passwordEncoder);
            seedAuditeur(utilisateurRepository, passwordEncoder);
            log.info(">>> Initialisation terminee (utilisateurs uniquement, sans seed demo)");
        };
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

    private void seedAdmin(UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder) {
        String email = "admin@tracabilite.ia";
        if (!utilisateurRepository.existsByEmail(email)) {
            Utilisateur admin = new Utilisateur();
            admin.setNom("Administrateur");
            admin.setEmail(email);
            admin.setMotDePasseHash(passwordEncoder.encode("admin123"));
            admin.setRole(RoleEnum.ADMINISTRATEUR);
            utilisateurRepository.save(admin);
            log.info(">>> Utilisateur admin cree : {} / admin123", email);
        } else {
            log.info(">>> Utilisateur admin deja present : {}", email);
        }
    }

    private void seedUser(UtilisateurRepository utilisateurRepository,
                          PasswordEncoder passwordEncoder) {
        String email = "user@tracabilite.ia";
        if (!utilisateurRepository.existsByEmail(email)) {
            Utilisateur user = new Utilisateur();
            user.setNom("Utilisateur");
            user.setEmail(email);
            user.setMotDePasseHash(passwordEncoder.encode("user123"));
            user.setRole(RoleEnum.UTILISATEUR);
            utilisateurRepository.save(user);
            log.info(">>> Utilisateur user cree : {} / user123", email);
        }
    }

    private void seedAuditeur(UtilisateurRepository utilisateurRepository,
                               PasswordEncoder passwordEncoder) {
        String email = "auditor@tracabilite.ia";
        if (!utilisateurRepository.existsByEmail(email)) {
            Utilisateur auditor = new Utilisateur();
            auditor.setNom("Auditeur");
            auditor.setEmail(email);
            auditor.setMotDePasseHash(passwordEncoder.encode("auditor123"));
            auditor.setRole(RoleEnum.AUDITEUR);
            utilisateurRepository.save(auditor);
            log.info(">>> Utilisateur auditeur cree : {} / auditor123", email);
        }
    }

    private void seedValidateur(UtilisateurRepository utilisateurRepository,
                                PasswordEncoder passwordEncoder) {
        String email = "validateur@tracabilite.ia";
        if (!utilisateurRepository.existsByEmail(email)) {
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
