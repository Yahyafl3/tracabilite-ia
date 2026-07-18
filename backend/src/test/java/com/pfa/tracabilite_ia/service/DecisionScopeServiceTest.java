package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionScopeServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private ReponseAgentIARepository reponseAgentIARepository;

    private DecisionScopeService service;

    @BeforeEach
    void setUp() {
        service = new DecisionScopeService(decisionRepository, reponseAgentIARepository);
    }

    @Test
    void loadForRead_attachesOnlyAgentsForRequestedDecision() {
        UUID decisionA = UUID.randomUUID();
        UUID decisionB = UUID.randomUUID();

        Decision decision = new Decision();
        decision.setDecisionId(decisionA);

        ReponseAgentIA agentA = agent(decisionA, "AGENT_1", "resume A");
        ReponseAgentIA agentB = agent(decisionB, "AGENT_2", "resume B");

        when(decisionRepository.findByIdWithFactors(decisionA)).thenReturn(Optional.of(decision));
        when(reponseAgentIARepository.findByDecisionDecisionIdOrderByAgentKeyAsc(decisionA))
                .thenReturn(List.of(agentA));

        Decision loaded = service.loadForRead(decisionA);

        assertThat(loaded.getReponsesAgents()).hasSize(1);
        assertThat(loaded.getReponsesAgents().get(0).getResume()).isEqualTo("resume A");
        assertThat(loaded.getReponsesAgents()).extracting(ReponseAgentIA::getResume)
                .doesNotContain("resume B");
        assertThat(agentB.getDecision().getDecisionId()).isEqualTo(decisionB);
    }

    @Test
    void loadForRead_throwsWhenDecisionMissing() {
        UUID missing = UUID.randomUUID();
        when(decisionRepository.findByIdWithFactors(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadForRead(missing))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ReponseAgentIA agent(UUID decisionId, String key, String resume) {
        Decision owner = new Decision();
        owner.setDecisionId(decisionId);
        ReponseAgentIA agent = new ReponseAgentIA();
        agent.setDecision(owner);
        agent.setAgentKey(key);
        agent.setResume(resume);
        agent.setStatut(StatutReponseAgentEnum.SUCCESS);
        return agent;
    }
}
