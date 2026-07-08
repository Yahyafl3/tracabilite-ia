package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreditAnalysisRequest;
import com.pfa.tracabilite_ia.dto.response.MLPredictionResponse;
import com.pfa.tracabilite_ia.entities.Decision;

public interface MLDecisionService {
    /**
     * Génère une décision automatique en utilisant le service ML
     */
    MLPredictionResponse genererDecisionML(CreditAnalysisRequest request);
    
    /**
     * Crée une décision et la sauvegarde à partir de la prédiction ML
     */
    Decision creerDecisionDepuisML(CreditAnalysisRequest request, Long systemeIaId);
    
    /**
     * Vérifie si le service ML est disponible
     */
    boolean isMLServiceAvailable();
}
