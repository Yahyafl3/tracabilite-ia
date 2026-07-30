package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.groq.GroqMultiAgentService;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.service.DomainAgentConsultationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DomainAgentConsultationServiceImpl implements DomainAgentConsultationService {

    private static final Logger log = LoggerFactory.getLogger(DomainAgentConsultationServiceImpl.class);

    private final GroqMultiAgentService groqMultiAgentService;
    private final DecisionHashService decisionHashService;

    public DomainAgentConsultationServiceImpl(
            GroqMultiAgentService groqMultiAgentService,
            DecisionHashService decisionHashService
    ) {
        this.groqMultiAgentService = groqMultiAgentService;
        this.decisionHashService = decisionHashService;
    }

    @Override
    public boolean consultAgents(
            Decision decision,
            DecisionDomain domain,
            String featuresJson,
            Utilisateur user
    ) {
        try {
            String prompt = buildPrompt(domain, decision);
            String contexte = buildContext(domain, decision, featuresJson);
            groqMultiAgentService.analyzeDecisionAgents(decision, prompt, contexte, user);
            decision.setAgentResponsesHash(
                    decisionHashService.computeAgentResponsesHash(decision.getReponsesAgents()));
            boolean consulted = decision.getReponsesAgents() != null && !decision.getReponsesAgents().isEmpty();
            if (!consulted) {
                log.info("Agents consultés mais aucune réponse pour décision {}", decision.getDecisionId());
            }
            return consulted;
        } catch (Exception ex) {
            log.warn("Agents IA indisponibles pour domaine {} decision {}: {}",
                    domain, decision.getDecisionId(), ex.getMessage());
            // Ne jamais altérer la prédiction ML
            return false;
        }
    }

    private String buildPrompt(DecisionDomain domain, Decision decision) {
        return switch (domain) {
            case CREDIT -> """
                    Tu es un agent consultatif de risque crédit (données synthétiques de démonstration).
                    Analyse la cohérence du dossier et propose des recommandations prudentes.
                    Ne remplace pas la prédiction ML fournie (""" + decision.getSuggestedDecision() + """
                    ).
                    Ne invente pas de scores SHAP. Indique clairement que ton avis est informatif.
                    """;
            case MEDICAL -> """
                    Tu es un agent consultatif d'aide à la décision (NON diagnostique).
                    Estimation ML indicative : """ + decision.getSuggestedDecision() + """
                    .
                    Formule des remarques prudentes, signale les limites, recommande une consultation
                    professionnelle. Ne propose AUCUN traitement automatique ni diagnostic définitif.
                    """;
            case EDUCATION -> """
                    Tu es un agent consultatif pédagogique.
                    Recommandation ML : """ + decision.getSuggestedDecision() + """
                    .
                    Propose des mesures d'accompagnement (tutorat, entretien).
                    Aucune sanction automatique contre l'étudiant.
                    Ton avis est informatif uniquement.
                    """;
        };
    }

    private String buildContext(DecisionDomain domain, Decision decision, String featuresJson) {
        String base = """
                Domaine=%s
                PredictionML=%s
                ConfianceML=%s
                Risque=%s
                ModelVersion=%s
                DatasetVersion=%s
                FeaturesJson=%s
                Les agents ne doivent PAS modifier prediction, probability, confidence ni facteurs ML.
                """.formatted(
                domain,
                decision.getSuggestedDecision(),
                decision.getConfidenceScore(),
                decision.getRiskLevel(),
                decision.getModelVersion(),
                decision.getDatasetVersion(),
                featuresJson != null && featuresJson.length() > 2000
                        ? featuresJson.substring(0, 2000) + "…"
                        : featuresJson
        );
        if (domain == DecisionDomain.MEDICAL) {
            base += " AVERTISSEMENT: estimation non diagnostique.";
        }
        return base;
    }
}
