package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.Utilisateur;

import java.util.UUID;

public interface DecisionService {

    Decision creer(DecisionRequest request);

    DecisionResponse obtenir(UUID id);

    DecisionResponse mettreAJour(UUID id, DecisionRequest request);

    DecisionPageResponse rechercher(String search, StatutDecisionEnum statut, int page, int size);

    DecisionResponse analyserCredit(CreditFeaturesRequest request);

    DecisionResponse retryFailedAgents(UUID id, Utilisateur user);
}
