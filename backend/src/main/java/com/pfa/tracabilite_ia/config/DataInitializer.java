package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedAdmin(UtilisateurRepository utilisateurRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
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
        };
    }
}
