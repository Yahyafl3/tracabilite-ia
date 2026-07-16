package com.pfa.tracabilite_ia.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.DecisionHistoryResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.DecisionHistory;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.DecisionHistoryRepository;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DecisionHistoryServiceImpl implements DecisionHistoryService {

    private static final String CORRELATION_ID_KEY = "correlationId";

    private final DecisionHistoryRepository decisionHistoryRepository;
    private final DecisionRepository decisionRepository;
    private final ObjectMapper objectMapper;

    public DecisionHistoryServiceImpl(DecisionHistoryRepository decisionHistoryRepository,
                                      DecisionRepository decisionRepository,
                                      ObjectMapper objectMapper) {
        this.decisionHistoryRepository = decisionHistoryRepository;
        this.decisionRepository = decisionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void record(Decision decision,
                       DecisionHistoryAction action,
                       StatutDecisionEnum previousStatus,
                       StatutDecisionEnum newStatus,
                       UUID performedById,
                       String performedByEmail,
                       String comment,
                       String justification,
                       Map<String, Object> eventData) {
        DecisionHistory history = new DecisionHistory();
        history.setDecision(decision);
        history.setAction(action);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setPerformedById(performedById);
        history.setPerformedByEmail(performedByEmail);
        history.setComment(comment);
        history.setJustification(justification);
        history.setEventDataJson(writeJson(eventData));
        history.setCorrelationId(MDC.get(CORRELATION_ID_KEY));
        decisionHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionHistoryResponse> listByDecision(UUID decisionId) {
        decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));
        return decisionHistoryRepository.findByDecisionDecisionIdOrderByCreatedAtAsc(decisionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DecisionHistoryResponse toResponse(DecisionHistory history) {
        return DecisionHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .decisionId(history.getDecision().getDecisionId())
                .action(history.getAction())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .performedById(history.getPerformedById())
                .performedByEmail(history.getPerformedByEmail())
                .comment(history.getComment())
                .justification(history.getJustification())
                .eventData(readEventData(history.getEventDataJson()))
                .correlationId(history.getCorrelationId())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private Map<String, Object> readEventData(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String writeJson(Map<String, Object> eventData) {
        if (eventData == null || eventData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(eventData);
        } catch (Exception ex) {
            throw new IllegalStateException("Erreur serialisation historique", ex);
        }
    }
}
