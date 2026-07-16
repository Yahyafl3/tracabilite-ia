package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionHistoryResponse {
    private UUID historyId;
    private UUID decisionId;
    private DecisionHistoryAction action;
    private StatutDecisionEnum previousStatus;
    private StatutDecisionEnum newStatus;
    private UUID performedById;
    private String performedByEmail;
    private String comment;
    private String justification;
    private Map<String, Object> eventData;
    private String correlationId;
    private LocalDateTime createdAt;
}
