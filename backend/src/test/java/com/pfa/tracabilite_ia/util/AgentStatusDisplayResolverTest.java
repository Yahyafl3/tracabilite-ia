package com.pfa.tracabilite_ia.util;

import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentStatusDisplayResolverTest {

    @Test
    void resolve_mapsRateLimitedCode() {
        ReponseAgentIA agent = new ReponseAgentIA();
        agent.setStatut(StatutReponseAgentEnum.FAILURE);
        agent.setCodeErreur("OPENROUTER_RATE_LIMITED");

        assertThat(AgentStatusDisplayResolver.resolve(agent)).isEqualTo("RATE_LIMITED");
    }

    @Test
    void resolve_mapsInvalidResponseCode() {
        ReponseAgentIA agent = new ReponseAgentIA();
        agent.setStatut(StatutReponseAgentEnum.INVALID_RESPONSE);
        agent.setCodeErreur("OPENROUTER_INVALID_RESPONSE");

        assertThat(AgentStatusDisplayResolver.resolve(agent)).isEqualTo("INVALID_RESPONSE");
    }
}
