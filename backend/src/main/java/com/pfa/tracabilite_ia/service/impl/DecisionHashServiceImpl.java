package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.util.HashUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DecisionHashServiceImpl implements DecisionHashService {

    @Override
    public String computeBusinessDataHash(String featuresJson) {
        if (featuresJson == null || featuresJson.isBlank()) {
            return HashUtils.sha256("");
        }
        return HashUtils.sha256(featuresJson.trim());
    }

    @Override
    public String computeSourcesHash(List<String> contentHashes) {
        if (contentHashes == null || contentHashes.isEmpty()) {
            return HashUtils.sha256("");
        }
        String joined = contentHashes.stream()
                .sorted()
                .collect(Collectors.joining("|"));
        return HashUtils.sha256(joined);
    }

    @Override
    public String computeAgentResponsesHash(List<ReponseAgentIA> responses) {
        if (responses == null || responses.isEmpty()) {
            return HashUtils.sha256("");
        }
        String joined = responses.stream()
                .sorted(Comparator.comparing(ReponseAgentIA::getAgentKey))
                .map(this::hashAgentResponse)
                .collect(Collectors.joining("|"));
        return HashUtils.sha256(joined);
    }

    /**
     * Délègue à l'entité : dupliquer la composition du payload ici ferait diverger
     * silencieusement la signature de la vérification au moindre champ ajouté.
     */
    @Override
    public String computeCanonicalHash(Decision decision) {
        return decision.calculerHash();
    }

    @Override
    public void refreshHashComponents(Decision decision, List<ReponseAgentIA> agentResponses) {
        decision.setBusinessDataHash(computeBusinessDataHash(decision.getFeaturesJson()));
        if (agentResponses != null) {
            decision.setAgentResponsesHash(computeAgentResponsesHash(agentResponses));
        }
        decision.setCurrentHash(computeCanonicalHash(decision));
    }

    @Override
    public boolean verifyDecisionIntegrity(Decision decision) {
        if (decision.getCurrentHash() == null) {
            return false;
        }
        // Source de vérité unique : Decision.calculerHash()
        return decision.getCurrentHash().equals(decision.calculerHash());
    }

    private String hashAgentResponse(ReponseAgentIA response) {
        String content = response.getStatut() == StatutReponseAgentEnum.SUCCESS
                ? response.getReponseBrute()
                : response.getCodeErreur() + ":" + response.getStatut();
        return HashUtils.sha256(response.getAgentKey() + "|" + response.getModelId() + "|" + safe(content));
    }

    private String safe(Object value) {
        return value != null ? value.toString() : "";
    }
}
