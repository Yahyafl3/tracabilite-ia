package com.pfa.tracabilite_ia.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterResponseParserTest {

    private OpenRouterResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new OpenRouterResponseParser(new ObjectMapper());
    }

    @Test
    void parse_declaredConfidenceNullWhenAbsent() {
        ParsedAgentResponse parsed = parser.parse(validJsonWithoutConfidence());

        assertThat(parsed.result().getConfidence()).isNull();
        assertThat(parsed.valid()).isTrue();
    }

    @Test
    void parse_declaredConfidenceRejectedWhenAboveOne() {
        ParsedAgentResponse parsed = parser.parse(jsonWithConfidence(1.5));

        assertThat(parsed.result().getConfidence()).isNull();
        assertThat(parsed.valid()).isTrue();
    }

    @Test
    void parse_declaredConfidenceRejectedWhenBelowZero() {
        ParsedAgentResponse parsed = parser.parse(jsonWithConfidence(-0.1));

        assertThat(parsed.result().getConfidence()).isNull();
        assertThat(parsed.valid()).isTrue();
    }

    @Test
    void parse_declaredConfidenceDifferentFromMlContextValue() {
        ParsedAgentResponse parsed = parser.parse(jsonWithConfidence(0.42));

        assertThat(parsed.result().getConfidence()).isEqualTo(0.42);
        assertThat(parsed.result().getConfidence()).isNotEqualTo(0.9338);
    }

    @Test
    void parse_preservesFrenchUtf8Characters() {
        ParsedAgentResponse parsed = parser.parse("""
                {
                  "suggestedDecision": "REVIEW",
                  "summary": "Analyse avec accents : éligibilité, créance, réévaluation",
                  "explanation": "Décision prudente à cause de l'endettement élevé"
                }
                """);

        assertThat(parsed.result().getSummary()).contains("éligibilité", "créance", "réévaluation");
        assertThat(parsed.result().getExplanation()).contains("Décision", "endettement élevé");
        assertThat(parsed.valid()).isTrue();
    }

    @Test
    void parse_removesInvalidControlCharactersFromNormalizedText() {
        ParsedAgentResponse parsed = parser.parse("""
                {
                  "suggestedDecision": "REVIEW",
                  "summary": "Résumé\u0007 avec\u0001 contrôle",
                  "explanation": "Explication valide"
                }
                """);

        assertThat(parsed.result().getSummary()).isEqualTo("Résumé avec contrôle");
        assertThat(parsed.valid()).isTrue();
    }

    @Test
    void parse_rawResponseIsNotModifiedByParser() {
        String raw = """
                {
                  "suggestedDecision": "REVIEW",
                  "summary": "Résumé\u0007 brut",
                  "explanation": "Explication"
                }
                """;

        parser.parse(raw);

        assertThat(raw).contains("\u0007");
    }

    @Test
    void parse_invalidSuggestedDecisionMarksResponseInvalid() {
        ParsedAgentResponse parsed = parser.parse("""
                {
                  "suggestedDecision": "MAYBE",
                  "summary": "Résumé valide",
                  "explanation": "Explication valide"
                }
                """);

        assertThat(parsed.valid()).isFalse();
        assertThat(parsed.result().getSuggestedDecision()).isNull();
    }

    @Test
    void parse_emptyTextMarksResponseInvalid() {
        ParsedAgentResponse parsed = parser.parse("""
                {
                  "suggestedDecision": "REVIEW",
                  "summary": "",
                  "explanation": "   "
                }
                """);

        assertThat(parsed.valid()).isFalse();
    }

    private String validJsonWithoutConfidence() {
        return """
                {
                  "suggestedDecision": "REVIEW",
                  "summary": "Résumé agent",
                  "explanation": "Explication agent"
                }
                """;
    }

    private String jsonWithConfidence(double confidence) {
        return """
                {
                  "suggestedDecision": "REVIEW",
                  "confidence": %s,
                  "summary": "Résumé agent",
                  "explanation": "Explication agent"
                }
                """.formatted(confidence);
    }
}
