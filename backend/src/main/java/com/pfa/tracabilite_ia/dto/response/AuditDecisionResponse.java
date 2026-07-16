package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDecisionResponse {
    private UUID decisionId;
    private String prompt;
    private String contexte;
    private String modelName;
    private String modelVersion;
    private String suggestedDecision;
    private Double confidenceScore;
    private String riskLevel;
    private String explanationSource;
    private String reponse;
    private ConsensusResponse consensus;
    private String resumeConsensus;
    private StatutDecisionEnum statutValidation;
    private String humanDecision;
    private String validatorEmail;
    private String businessDataHash;
    private String sourcesHash;
    private String agentResponsesHash;
    private String previousHash;
    private String currentHash;
    private boolean integrityValid;
    private LocalDateTime timestamp;
    private List<ReponseAgentResponse> agentResponses;
    private List<ValidationActionResponse> validations;
    private List<DecisionHistoryResponse> history;
    private List<DecisionSourceResponse> sources;
}
