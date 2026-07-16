package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionHashServiceTest {

    private DecisionHashServiceImpl hashService;

    @BeforeEach
    void setUp() {
        hashService = new DecisionHashServiceImpl();
    }

    @Test
    void verifyDecisionIntegrity_validAfterSave() {
        Decision decision = sampleDecision();
        hashService.refreshHashComponents(decision, List.of());
        decision.setCurrentHash(hashService.computeCanonicalHash(decision));

        assertThat(hashService.verifyDecisionIntegrity(decision)).isTrue();
    }

    @Test
    void verifyDecisionIntegrity_invalidAfterTampering() {
        Decision decision = sampleDecision();
        hashService.refreshHashComponents(decision, List.of());
        decision.setCurrentHash(hashService.computeCanonicalHash(decision));

        decision.setSuggestedDecision("REJETER");

        assertThat(hashService.verifyDecisionIntegrity(decision)).isFalse();
    }

    @Test
    void computeAgentResponsesHash_changesWhenAgentResponseChanges() {
        ReponseAgentIA agent = new ReponseAgentIA();
        agent.setAgentKey("AGENT_1");
        agent.setModelId("meta-llama/test");
        agent.setStatut(StatutReponseAgentEnum.SUCCESS);
        agent.setReponseBrute("{\"decision\":\"APPROUVER\"}");

        String first = hashService.computeAgentResponsesHash(List.of(agent));
        agent.setReponseBrute("{\"decision\":\"REJETER\"}");
        String second = hashService.computeAgentResponsesHash(List.of(agent));

        assertThat(first).isNotEqualTo(second);
    }

    private Decision sampleDecision() {
        Decision decision = new Decision();
        decision.setDecisionId(UUID.randomUUID());
        decision.setPrompt("Demande credit");
        decision.setContexte("Credit test");
        decision.setModelName("LogisticRegression");
        decision.setModelVersion("2.0.0");
        decision.setSuggestedDecision("APPROUVER");
        decision.setConfidenceScore(91.5);
        decision.setFeaturesJson("{\"amount\":10000}");
        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE);
        decision.setSourcesHash(hashService.computeSourcesHash(List.of()));
        return decision;
    }
}
