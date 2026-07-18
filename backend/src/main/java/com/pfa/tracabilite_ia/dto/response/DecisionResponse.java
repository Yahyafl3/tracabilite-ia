package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionResponse {
    private UUID decisionId;
    private String reference;
    private String prompt;
    private String contexte;
    private String modelName;
    private String modelVersion;
    private String reponse;
    private StatutDecisionEnum statutValidation;
    private String suggestedDecision;
    private Double confidenceScore;
    private String riskLevel;
    private String explanationSource;
    private String resumeConsensus;
    private ConsensusResponse consensus;
    private String consensusDecision;
    private MlPredictionView mlPrediction;
    private List<ReponseAgentResponse> agentResponses;
    private Map<String, Object> features;
    private Map<String, Double> probabilities;
    private List<ExplanationFactorResponse> factors;
    private List<ValidationActionResponse> validations;
    private String humanFinalDecision;
    private TypeActionEnum humanFinalAction;
    private String validatorEmail;
    private LocalDateTime validatedAt;
    private LocalDateTime timestamp;
    private String currentHash;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExplanationFactorResponse {
        private UUID factorId;
        private String name;
        private String value;
        private Double shapValue;
        private String impact;
        private Integer rank;
        private Double contributionPercent;
        private String source;
    }
}
