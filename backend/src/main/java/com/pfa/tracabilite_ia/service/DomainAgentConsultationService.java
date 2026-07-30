package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;

/**
 * Consultation facultative des agents IA — jamais de modification du résultat ML.
 */
public interface DomainAgentConsultationService {

    /**
     * Enrichit la décision avec consensus / réponses agents si les LLM sont disponibles.
     * En cas d'échec : ne lève pas ; laisse la prédiction ML intacte.
     *
     * @return true si au moins un agent a répondu
     */
    boolean consultAgents(Decision decision, DecisionDomain domain, String featuresJson, Utilisateur user);
}
