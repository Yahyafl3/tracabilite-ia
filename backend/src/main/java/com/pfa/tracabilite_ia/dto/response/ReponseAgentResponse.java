package com.pfa.tracabilite_ia.dto.response;

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
public class ReponseAgentResponse {
    private UUID reponseAgentId;
    private String agentKey;
    private String modelId;
    private String modelName;
    private String provider;
    private String decisionProposee;
    private Double confianceDeclaree;
    private String niveauRisque;
    private String resume;
    private String explication;
    private List<String> recommandations;
    private Long dureeMs;
    private Integer nombreTokens;
    private StatutReponseAgentEnum statut;
    private String codeErreur;
    private LocalDateTime timestamp;
}
