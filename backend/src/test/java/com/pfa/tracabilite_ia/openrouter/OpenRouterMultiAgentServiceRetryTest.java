package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterMultiAgentServiceRetryTest {

    @Test
    void isRetryableFailure_acceptsRateLimitedTimeoutAndUnavailable() {
        ReponseAgentIA rateLimited = new ReponseAgentIA();
        rateLimited.setStatut(StatutReponseAgentEnum.FAILURE);
        rateLimited.setFallbackReason("RATE_LIMITED");

        ReponseAgentIA timeout = new ReponseAgentIA();
        timeout.setStatut(StatutReponseAgentEnum.TIMEOUT);
        timeout.setFallbackReason("TIMEOUT");

        ReponseAgentIA unavailable = new ReponseAgentIA();
        unavailable.setStatut(StatutReponseAgentEnum.FAILURE);
        unavailable.setFallbackReason("TEMPORARILY_UNAVAILABLE");

        assertThat(OpenRouterMultiAgentService.isRetryableFailure(rateLimited)).isTrue();
        assertThat(OpenRouterMultiAgentService.isRetryableFailure(timeout)).isTrue();
        assertThat(OpenRouterMultiAgentService.isRetryableFailure(unavailable)).isTrue();
    }

    @Test
    void isRetryableFailure_rejectsSuccessAndAuthErrors() {
        ReponseAgentIA success = new ReponseAgentIA();
        success.setStatut(StatutReponseAgentEnum.SUCCESS);

        ReponseAgentIA auth = new ReponseAgentIA();
        auth.setStatut(StatutReponseAgentEnum.FAILURE);
        auth.setCodeErreur("OPENROUTER_AUTHENTICATION_FAILED");

        assertThat(OpenRouterMultiAgentService.isRetryableFailure(success)).isFalse();
        assertThat(OpenRouterMultiAgentService.isRetryableFailure(auth)).isFalse();
    }
}
