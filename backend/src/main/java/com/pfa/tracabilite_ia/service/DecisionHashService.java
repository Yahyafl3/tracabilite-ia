package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;

import java.util.List;

public interface DecisionHashService {

    String computeBusinessDataHash(String featuresJson);

    String computeSourcesHash(List<String> contentHashes);

    String computeAgentResponsesHash(List<ReponseAgentIA> responses);

    String computeCanonicalHash(Decision decision);

    void refreshHashComponents(Decision decision, List<ReponseAgentIA> agentResponses);

    boolean verifyDecisionIntegrity(Decision decision);
}
