package com.pfa.tracabilite_ia.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReponseAgentResponse {
    private UUID reponseAgentId;
    private String agentKey;
    private String modelId;
    @JsonAlias("modelName")
    private String displayName;
    @JsonIgnore
    private String modelName;
    private String provider;
    private String decisionProposee;
    @JsonAlias("confianceDeclaree")
    private Double declaredConfidence;
    @JsonIgnore
    private Double confianceDeclaree;
    private String niveauRisque;
    private String resume;
    private String explication;
    private List<String> recommandations;
    private Long dureeMs;
    private Integer nombreTokens;
    private StatutReponseAgentEnum statut;
    private String displayStatus;
    private String codeErreur;
    private String requestedModelId;
    private String actualModelId;
    private Boolean fallbackUsed;
    private String fallbackReason;
    private String responseHash;
    private Integer retryCount;
    private String fallbackMessage;
    private LocalDateTime timestamp;
}
