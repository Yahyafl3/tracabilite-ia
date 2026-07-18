package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionIsolationTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private ReponseAgentIARepository reponseAgentIARepository;

    private DecisionScopeService scopeService;

    @BeforeEach
    void setUp() {
        scopeService = new DecisionScopeService(decisionRepository, reponseAgentIARepository);
    }

    @Test
    void twoDecisionsHaveDifferentScopedAgentResponses() {
        UUID decisionA = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID decisionB = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Decision entityA = decision(decisionA);
        Decision entityB = decision(decisionB);

        ReponseAgentIA agentA = agent(entityA, "AGENT_2", "REJETER", "Gemma A");
        ReponseAgentIA agentB = agent(entityB, "AGENT_2", "REVIEW", "Gemma B");

        when(decisionRepository.findByIdWithFactors(decisionA)).thenReturn(Optional.of(entityA));
        when(decisionRepository.findByIdWithFactors(decisionB)).thenReturn(Optional.of(entityB));
        when(reponseAgentIARepository.findByDecisionDecisionIdOrderByAgentKeyAsc(decisionA))
                .thenReturn(List.of(agentA));
        when(reponseAgentIARepository.findByDecisionDecisionIdOrderByAgentKeyAsc(decisionB))
                .thenReturn(List.of(agentB));

        Decision loadedA = scopeService.loadForRead(decisionA);
        Decision loadedB = scopeService.loadForRead(decisionB);

        assertThat(loadedA.getReponsesAgents()).extracting(ReponseAgentIA::getResume).containsExactly("Gemma A");
        assertThat(loadedB.getReponsesAgents()).extracting(ReponseAgentIA::getResume).containsExactly("Gemma B");
        assertThat(loadedA.getReponsesAgents()).extracting(ReponseAgentIA::getDecisionProposee).doesNotContain("REVIEW");
        assertThat(loadedB.getReponsesAgents()).extracting(ReponseAgentIA::getDecisionProposee).doesNotContain("REJETER");
    }

    private Decision decision(UUID id) {
        Decision decision = new Decision();
        decision.setDecisionId(id);
        return decision;
    }

    private ReponseAgentIA agent(Decision decision, String key, String proposed, String resume) {
        ReponseAgentIA agent = new ReponseAgentIA();
        agent.setDecision(decision);
        agent.setAgentKey(key);
        agent.setDecisionProposee(proposed);
        agent.setResume(resume);
        agent.setStatut(StatutReponseAgentEnum.SUCCESS);
        return agent;
    }
}
