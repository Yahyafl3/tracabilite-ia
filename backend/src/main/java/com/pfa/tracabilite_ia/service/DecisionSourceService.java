package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.request.CreateDecisionSourceRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionSourceResponse;
import com.pfa.tracabilite_ia.entities.Decision;

import java.util.List;
import java.util.UUID;

public interface DecisionSourceService {

    void createDefaultSources(Decision decision, String featuresJson, String prompt,
                              UUID createdById, String createdByEmail);

    List<DecisionSourceResponse> listByDecision(UUID decisionId);

    DecisionSourceResponse addSource(UUID decisionId, CreateDecisionSourceRequest request,
                                     UUID createdById, String createdByEmail);

    void removeSource(UUID decisionId, UUID sourceId, UUID performedById, String performedByEmail);

    String computeSourcesHash(UUID decisionId);

    void refreshSourcesHash(Decision decision);
}
