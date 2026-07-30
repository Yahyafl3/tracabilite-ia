package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DecisionService {

    Decision creer(DecisionRequest request);

    DecisionResponse obtenir(UUID id);

    DecisionResponse mettreAJour(UUID id, DecisionRequest request);

    DecisionPageResponse rechercher(String search, StatutDecisionEnum statut, int page, int size);

    DecisionPageResponse rechercher(
            String search,
            StatutDecisionEnum statut,
            DecisionDomain domaine,
            String riskLevel,
            String decisionFinale,
            String validateur,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    );

    DecisionResponse analyserCredit(CreditFeaturesRequest request);

    DecisionResponse retryFailedAgents(UUID id, Utilisateur user);
}
