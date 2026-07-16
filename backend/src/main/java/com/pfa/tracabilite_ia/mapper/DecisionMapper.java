package com.pfa.tracabilite_ia.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.ConsensusResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ExplanationFactor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DecisionMapper {

    private final ObjectMapper objectMapper;
    private final ReponseAgentMapper reponseAgentMapper;

    public DecisionMapper(ObjectMapper objectMapper, ReponseAgentMapper reponseAgentMapper) {
        this.objectMapper = objectMapper;
        this.reponseAgentMapper = reponseAgentMapper;
    }

    public DecisionResponse toResponse(Decision decision) {
        return DecisionResponse.builder()
                .decisionId(decision.getDecisionId())
                .prompt(decision.getPrompt())
                .contexte(decision.getContexte())
                .modelName(decision.getModelName())
                .modelVersion(decision.getModelVersion())
                .reponse(decision.getReponse())
                .statutValidation(decision.getStatutValidation())
                .suggestedDecision(decision.getSuggestedDecision())
                .confidenceScore(decision.getConfidenceScore())
                .riskLevel(decision.getRiskLevel())
                .explanationSource(decision.getExplanationSource())
                .resumeConsensus(decision.getResumeConsensus())
                .consensus(readConsensus(decision.getConsensusJson()))
                .agentResponses(reponseAgentMapper.toResponseList(decision.getReponsesAgents()))
                .features(readFeatures(decision.getFeaturesJson()))
                .probabilities(readProbabilities(decision.getProbabilitiesJson()))
                .factors(mapFactors(decision.getExplanationFactors()))
                .timestamp(decision.getTimestamp())
                .currentHash(decision.getCurrentHash())
                .build();
    }

    public List<DecisionResponse> toResponseList(List<Decision> decisions) {
        return decisions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ConsensusResponse readConsensus(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ConsensusResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> readFeatures(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Double> readProbabilities(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private List<DecisionResponse.ExplanationFactorResponse> mapFactors(List<ExplanationFactor> factors) {
        if (factors == null) {
            return Collections.emptyList();
        }
        return factors.stream()
                .sorted((left, right) -> Integer.compare(left.getRank(), right.getRank()))
                .map(factor -> DecisionResponse.ExplanationFactorResponse.builder()
                        .factorId(factor.getFactorId())
                        .name(factor.getName())
                        .value(factor.getValue())
                        .shapValue(factor.getShapValue())
                        .impact(factor.getImpact())
                        .rank(factor.getRank())
                        .contributionPercent(factor.getContributionPercent())
                        .source(factor.getSource())
                        .build())
                .collect(Collectors.toList());
    }
}
