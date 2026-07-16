package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.DecisionHistoryResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DecisionHistoryService {

    void record(Decision decision,
                DecisionHistoryAction action,
                StatutDecisionEnum previousStatus,
                StatutDecisionEnum newStatus,
                UUID performedById,
                String performedByEmail,
                String comment,
                String justification,
                Map<String, Object> eventData);

    List<DecisionHistoryResponse> listByDecision(UUID decisionId);
}
