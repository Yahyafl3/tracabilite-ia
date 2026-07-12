package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.entities.Decision;

import java.util.List;
import java.util.UUID;

public interface DecisionService {

    Decision creer(DecisionRequest request);

    List<Decision> lister();

    Decision obtenir(UUID id);

    Decision mettreAJour(UUID id, DecisionRequest request);
}
