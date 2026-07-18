package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.dto.response.ValidationActionResponse;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
import com.pfa.tracabilite_ia.mapper.DecisionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DecisionMapperValidationMetadataTest {

    private DecisionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DecisionMapper(new com.fasterxml.jackson.databind.ObjectMapper(),
                new com.pfa.tracabilite_ia.mapper.ReponseAgentMapper(new com.fasterxml.jackson.databind.ObjectMapper()));
    }

    @Test
    void applyValidationMetadata_setsHumanFinalActionFromLatestValidation() {
        UUID decisionId = UUID.randomUUID();
        DecisionResponse response = DecisionResponse.builder().decisionId(decisionId).build();
        ValidationActionResponse latest = ValidationActionResponse.builder()
                .typeAction(TypeActionEnum.MODIFIER)
                .decisionHumaine("REJETER")
                .build();

        mapper.applyValidationMetadata(response, List.of(latest));

        assertThat(response.getHumanFinalAction()).isEqualTo(TypeActionEnum.MODIFIER);
        assertThat(response.getHumanFinalDecision()).isEqualTo("REJETER");
    }

    @Test
    void applyValidationMetadata_keepsMlSeparateFromHumanDecision() {
        DecisionResponse response = DecisionResponse.builder()
                .suggestedDecision("APPROUVER")
                .mlPrediction(com.pfa.tracabilite_ia.dto.response.MlPredictionView.builder()
                        .decision("APPROUVER")
                        .confidenceScore(91.0)
                        .build())
                .build();

        mapper.applyValidationMetadata(response, List.of(ValidationActionResponse.builder()
                .typeAction(TypeActionEnum.REJETER)
                .decisionHumaine("REJETER")
                .build()));

        assertThat(response.getSuggestedDecision()).isEqualTo("APPROUVER");
        assertThat(response.getMlPrediction().getDecision()).isEqualTo("APPROUVER");
        assertThat(response.getHumanFinalDecision()).isEqualTo("REJETER");
    }
}
