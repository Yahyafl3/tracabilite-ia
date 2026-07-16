package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.ValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.dto.response.ValidationActionResponse;

import java.util.List;
import java.util.UUID;

public interface ValidationService {

    DecisionPageResponse listerEnAttente(int page, int size);

    List<ValidationActionResponse> historique(UUID decisionId);

    DecisionResponse approuver(UUID decisionId, ValidationRequest request);

    DecisionResponse rejeter(UUID decisionId, ValidationRequest request);

    DecisionResponse modifier(UUID decisionId, ValidationRequest request);
}
