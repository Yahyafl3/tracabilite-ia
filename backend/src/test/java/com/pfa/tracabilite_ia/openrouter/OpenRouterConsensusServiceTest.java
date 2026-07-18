package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.dto.response.ConsensusResponse;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterConsensusServiceTest {

    private final OpenRouterConsensusService service = new OpenRouterConsensusService();

    @Test
    void compute_zeroSuccessfulResponses_returnsInsufficientResponsesWithoutReviewFallback() {
        ReponseAgentIA failed1 = response(null, StatutReponseAgentEnum.FAILURE, null);
        ReponseAgentIA failed2 = response(null, StatutReponseAgentEnum.MODEL_UNAVAILABLE, null);
        ReponseAgentIA failed3 = response(null, StatutReponseAgentEnum.FAILURE, null);

        ConsensusResponse consensus = service.compute(List.of(failed1, failed2, failed3));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("INSUFFICIENT_RESPONSES");
        assertThat(consensus.getDecisionConsensus()).isNotEqualTo("REVIEW");
        assertThat(consensus.getAgreementRate()).isNull();
        assertThat(consensus.isConsensusAvailable()).isFalse();
        assertThat(consensus.getAgentsReussis()).isZero();
        assertThat(consensus.getSuccessfulAgentCount()).isZero();
    }

    @Test
    void compute_oneSuccessfulResponse_returnsInsufficientResponses() {
        ReponseAgentIA ok = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.8);
        ReponseAgentIA failed = response(null, StatutReponseAgentEnum.FAILURE, null);

        ConsensusResponse consensus = service.compute(List.of(ok, failed, failed));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("INSUFFICIENT_RESPONSES");
        assertThat(consensus.getAgreementRate()).isNull();
        assertThat(consensus.isConsensusAvailable()).isFalse();
        assertThat(consensus.getSuccessfulAgentCount()).isEqualTo(1);
    }

    @Test
    void compute_twoIdenticalSuccessfulResponses_returnsMajorityWithFullAgreement() {
        ReponseAgentIA ok1 = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.8);
        ReponseAgentIA ok2 = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.6);
        ReponseAgentIA failed = response(null, StatutReponseAgentEnum.FAILURE, null);

        ConsensusResponse consensus = service.compute(List.of(ok1, ok2, failed));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("APPROUVER");
        assertThat(consensus.getAgreementRate()).isEqualTo(100.0);
        assertThat(consensus.isConsensusAvailable()).isTrue();
        assertThat(consensus.getSuccessfulAgentCount()).isEqualTo(2);
    }

    @Test
    void compute_twoDifferentSuccessfulResponses_returnsNoConsensus() {
        ReponseAgentIA ok1 = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.8);
        ReponseAgentIA ok2 = response("REJETER", StatutReponseAgentEnum.SUCCESS, 0.6);
        ReponseAgentIA failed = response(null, StatutReponseAgentEnum.FAILURE, null);

        ConsensusResponse consensus = service.compute(List.of(ok1, ok2, failed));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("NO_CONSENSUS");
        assertThat(consensus.getAgreementRate()).isEqualTo(50.0);
        assertThat(consensus.isConsensusAvailable()).isFalse();
    }

    @Test
    void compute_majorityTwoAgainstOne_returnsMajorityDecision() {
        ReponseAgentIA review1 = response("REVIEW", StatutReponseAgentEnum.SUCCESS, 0.7);
        ReponseAgentIA review2 = response("REVIEW", StatutReponseAgentEnum.SUCCESS, 0.6);
        ReponseAgentIA approve = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.9);

        ConsensusResponse consensus = service.compute(List.of(review1, review2, approve));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("REVIEW");
        assertThat(consensus.getAgreementRate()).isEqualTo(66.67);
        assertThat(consensus.isConsensusAvailable()).isTrue();
    }

    @Test
    void compute_threeDifferentSuccessfulResponses_returnsNoConsensus() {
        ReponseAgentIA approve = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.8);
        ReponseAgentIA reject = response("REJETER", StatutReponseAgentEnum.SUCCESS, 0.7);
        ReponseAgentIA review = response("REVIEW", StatutReponseAgentEnum.SUCCESS, 0.6);

        ConsensusResponse consensus = service.compute(List.of(approve, reject, review));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("NO_CONSENSUS");
        assertThat(consensus.getAgreementRate()).isEqualTo(33.33);
        assertThat(consensus.isConsensusAvailable()).isFalse();
    }

    @Test
    void compute_rejectVersusReviewWithTwoAgents_returnsNoConsensus() {
        ReponseAgentIA reject = response("REJETER", StatutReponseAgentEnum.SUCCESS, 0.7);
        ReponseAgentIA review = response("REVIEW", StatutReponseAgentEnum.SUCCESS, 0.6);
        ReponseAgentIA failed = response(null, StatutReponseAgentEnum.FAILURE, null);

        ConsensusResponse consensus = service.compute(List.of(reject, review, failed));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("NO_CONSENSUS");
        assertThat(consensus.isConsensusAvailable()).isFalse();
    }

    @Test
    void compute_rejectVersusReview_returnsNoConsensus() {
        ReponseAgentIA reject = response("REJETER", StatutReponseAgentEnum.SUCCESS, 0.7);
        ReponseAgentIA review = response("REVIEW", StatutReponseAgentEnum.SUCCESS, 0.6);
        ReponseAgentIA approve = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.8);

        ConsensusResponse consensus = service.compute(List.of(reject, review, approve));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("NO_CONSENSUS");
        assertThat(consensus.isConsensusAvailable()).isFalse();
    }

    @Test
    void compute_invalidResponseAgentsAreExcludedFromConsensus() {
        ReponseAgentIA invalid = response("REVIEW", StatutReponseAgentEnum.INVALID_RESPONSE, null);
        ReponseAgentIA ok1 = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.8);
        ReponseAgentIA ok2 = response("APPROUVER", StatutReponseAgentEnum.SUCCESS, 0.7);

        ConsensusResponse consensus = service.compute(List.of(invalid, ok1, ok2));

        assertThat(consensus.getDecisionConsensus()).isEqualTo("APPROUVER");
        assertThat(consensus.getSuccessfulAgentCount()).isEqualTo(2);
    }

    @Test
    void buildSkippedConsensus_returnsQuotaMessage() {
        ConsensusResponse consensus = service.buildSkippedConsensus(
                "Quota OpenRouter insuffisant. L'analyse ML reste disponible.");

        assertThat(consensus.getDecisionConsensus()).isEqualTo("INSUFFICIENT_RESPONSES");
        assertThat(consensus.isConsensusAvailable()).isFalse();
        assertThat(consensus.getResume()).contains("Quota OpenRouter insuffisant");
    }

    private ReponseAgentIA response(String decision, StatutReponseAgentEnum statut, Double confidence) {
        ReponseAgentIA entity = new ReponseAgentIA();
        entity.setDecisionProposee(decision);
        entity.setStatut(statut);
        entity.setConfianceDeclaree(confidence);
        entity.setResume("resume");
        return entity;
    }
}
