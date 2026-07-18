package com.pfa.tracabilite_ia.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.ConsensusResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.dto.response.MlPredictionView;
import com.pfa.tracabilite_ia.dto.response.ValidationActionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ExplanationFactor;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
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
        ConsensusResponse consensus = readConsensus(decision.getConsensusJson());
        return DecisionResponse.builder()
                .decisionId(decision.getDecisionId())
                .reference(formatReference(decision.getDecisionId()))
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
                .consensus(consensus)
                .consensusDecision(consensus != null ? consensus.getDecisionConsensus() : null)
                .mlPrediction(buildMlPrediction(decision))
                .agentResponses(reponseAgentMapper.toResponseList(decision.getReponsesAgents()))
                .features(readFeatures(decision.getFeaturesJson()))
                .probabilities(readProbabilities(decision.getProbabilitiesJson()))
                .factors(mapFactors(decision.getExplanationFactors()))
                .humanFinalDecision(decision.getHumanDecision())
                .validatorEmail(decision.getValidatorEmail())
                .timestamp(decision.getTimestamp())
                .currentHash(decision.getCurrentHash())
                .build();
    }

    public void applyValidationMetadata(DecisionResponse response, List<ValidationActionResponse> validations) {
        response.setValidations(validations);
        if (validations == null || validations.isEmpty()) {
            return;
        }
        ValidationActionResponse latest = validations.get(0);
        response.setValidatedAt(latest.getTimestamp());
        response.setHumanFinalAction(latest.getTypeAction());
        if (latest.getTypeAction() == TypeActionEnum.MODIFIER) {
            response.setHumanFinalDecision(latest.getDecisionHumaine());
        } else if (latest.getTypeAction() == TypeActionEnum.REVIEW) {
            response.setHumanFinalDecision("REVIEW");
        } else if (response.getHumanFinalDecision() == null) {
            response.setHumanFinalDecision(latest.getDecisionHumaine());
        }
    }

    public List<DecisionResponse> toResponseList(List<Decision> decisions) {
        return decisions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private MlPredictionView buildMlPrediction(Decision decision) {
        return MlPredictionView.builder()
                .decision(decision.getSuggestedDecision())
                .confidenceScore(decision.getConfidenceScore())
                .riskLevel(decision.getRiskLevel())
                .modelName(decision.getModelName())
                .modelVersion(decision.getModelVersion())
                .build();
    }

    private String formatReference(java.util.UUID decisionId) {
        if (decisionId == null) {
            return null;
        }
        return decisionId.toString().substring(0, 8).toUpperCase();
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
