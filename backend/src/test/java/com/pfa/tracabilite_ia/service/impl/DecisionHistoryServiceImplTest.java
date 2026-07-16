package com.pfa.tracabilite_ia.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.DecisionHistoryRepository;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionHistoryServiceImplTest {

    @Mock
    private DecisionHistoryRepository decisionHistoryRepository;

    @Mock
    private DecisionRepository decisionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void record_persistsHistoryEntry() {
        DecisionHistoryServiceImpl service = new DecisionHistoryServiceImpl(
                decisionHistoryRepository, decisionRepository, objectMapper);

        Decision decision = new Decision();
        decision.setDecisionId(UUID.randomUUID());
        decision.setStatutValidation(StatutDecisionEnum.BROUILLON);

        service.record(decision, DecisionHistoryAction.DECISION_CREATED, null,
                StatutDecisionEnum.BROUILLON, UUID.randomUUID(), "user@test.ia",
                null, null, Map.of("source", "test"));

        ArgumentCaptor<com.pfa.tracabilite_ia.entities.DecisionHistory> captor =
                ArgumentCaptor.forClass(com.pfa.tracabilite_ia.entities.DecisionHistory.class);
        verify(decisionHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAction()).isEqualTo(DecisionHistoryAction.DECISION_CREATED);
    }

    @Test
    void listByDecision_returnsEntries() {
        DecisionHistoryServiceImpl service = new DecisionHistoryServiceImpl(
                decisionHistoryRepository, decisionRepository, objectMapper);
        UUID decisionId = UUID.randomUUID();
        when(decisionRepository.findById(decisionId)).thenReturn(Optional.of(new Decision()));
        when(decisionHistoryRepository.findByDecisionDecisionIdOrderByCreatedAtAsc(decisionId))
                .thenReturn(java.util.List.of());

        assertThat(service.listByDecision(decisionId)).isEmpty();
    }
}
