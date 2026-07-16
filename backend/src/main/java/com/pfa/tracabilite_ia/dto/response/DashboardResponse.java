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
public class DashboardResponse {

    private long totalDecisions;
    private long approuvees;
    private long modifiees;
    private long rejetees;
    private long enAttente;
    private long brouillon;
    private double tauxValidation;
    private int agentsActifs;
    private String agentsLabel;
    private boolean hashChainIntact;
    private LocalDateTime generatedAt;
    private List<RecentDecisionSummary> recentDecisions;
    private List<ComparaisonAgentResponse> agentPerformance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentDecisionSummary {
        private UUID decisionId;
        private String prompt;
        private String modelName;
        private String agentLabel;
        private StatutDecisionEnum statutValidation;
        private LocalDateTime timestamp;
    }
}
