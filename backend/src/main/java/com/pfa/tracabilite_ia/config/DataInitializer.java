package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.SystemeIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.RoleEnum;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.SystemeIARepository;
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
    public CommandLineRunner seedDemoData(UtilisateurRepository utilisateurRepository,
                                       SystemeIARepository systemeIARepository,
                                       DecisionRepository decisionRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            seedAdmin(utilisateurRepository, passwordEncoder);

            SystemeIA chatgpt = seedSystemeIA(systemeIARepository,
                    "ChatGPT", "OpenAI", "gpt-4.1", "Modèle conversationnel généraliste");
            SystemeIA claude = seedSystemeIA(systemeIARepository,
                    "Claude", "Anthropic", "claude-sonnet-4", "Modèle orienté raisonnement et rédaction");
            SystemeIA gemini = seedSystemeIA(systemeIARepository,
                    "Gemini", "Google", "gemini-2.5", "Modèle multimodal pour assistants et recherche");

            if (decisionRepository.count() == 0) {
                Decision last = null;
                last = seedDecision(decisionRepository, chatgpt, "Analyse de support étudiant", StatutDecisionEnum.APPROUVEE, last);
                last = seedDecision(decisionRepository, chatgpt, "Réponse pédagogique sur Java", StatutDecisionEnum.APPROUVEE, last);
                last = seedDecision(decisionRepository, chatgpt, "Synthèse d'un article", StatutDecisionEnum.MODIFIEE, last);

                last = seedDecision(decisionRepository, claude, "Correction d'un devoir", StatutDecisionEnum.APPROUVEE, last);
                last = seedDecision(decisionRepository, claude, "Explication de SQL", StatutDecisionEnum.REJETEE, last);
                last = seedDecision(decisionRepository, claude, "Génération de résumé", StatutDecisionEnum.REJETEE, last);

                last = seedDecision(decisionRepository, gemini, "Aide à la programmation", StatutDecisionEnum.APPROUVEE, last);
                last = seedDecision(decisionRepository, gemini, "Révision de contenu", StatutDecisionEnum.MODIFIEE, last);
                seedDecision(decisionRepository, gemini, "Réponse multi-étapes", StatutDecisionEnum.REJETEE, last);
                log.info(">>> Donnees de demo de comparaison IA creees");
            } else {
                log.info(">>> Des decisions existent deja, le seed de demo a ete ignore");
            }
        };
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

    private SystemeIA seedSystemeIA(SystemeIARepository systemeIARepository,
                                    String nom,
                                    String fournisseur,
                                    String modele,
                                    String description) {
        return systemeIARepository.findByNomIgnoreCaseAndFournisseurIgnoreCase(nom, fournisseur)
                .orElseGet(() -> {
                    SystemeIA systemeIA = new SystemeIA();
                    systemeIA.setNom(nom);
                    systemeIA.setFournisseur(fournisseur);
                    systemeIA.setModele(modele);
                    systemeIA.setVersionModele("latest");
                    systemeIA.setDescription(description);
                    systemeIA.setActif(Boolean.TRUE);
                    SystemeIA saved = systemeIARepository.save(systemeIA);
                    log.info(">>> Systeme IA cree : {} / {}", nom, fournisseur);
                    return saved;
                });
    }

    private Decision seedDecision(DecisionRepository decisionRepository,
                                  SystemeIA systemeIA,
                                  String prompt,
                                  StatutDecisionEnum statut,
                                  Decision previous) {
        Decision decision = new Decision();
        decision.setPrompt(prompt);
        decision.setContexte("Contexte de demonstration pour " + systemeIA.getNom());
        decision.setModelName(systemeIA.getNom());
        decision.setModelVersion(systemeIA.getVersionModele());
        decision.setReponse("Reponse de demo pour " + systemeIA.getNom());
        decision.setSystemeIa(systemeIA);
        decision.setStatutValidation(statut);
        decision.setDecisionPrecedente(previous);
        decision.setPreviousHash(previous != null ? previous.getCurrentHash() : null);

        Decision saved = decisionRepository.save(decision);
        saved.setCurrentHash(saved.calculerHash());
        Decision updated = decisionRepository.save(saved);
        log.info(">>> Decision de demo creee pour {} avec statut {}", systemeIA.getNom(), statut);
        return updated;
    }
}
