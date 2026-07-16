package com.pfa.tracabilite_ia.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenRouterResponseParser {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterResponseParser.class);
    private static final Pattern JSON_BLOCK = Pattern.compile("\\{.*}", Pattern.DOTALL);

    private final ObjectMapper objectMapper;

    public OpenRouterResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AIAnalysisResult parse(String rawContent) {
        String jsonPayload = extractJsonPayload(rawContent);
        try {
            JsonNode node = objectMapper.readTree(jsonPayload);
            AIAnalysisResult result = new AIAnalysisResult();
            result.setSuggestedDecision(normalizeDecision(readText(node, "suggestedDecision", "REVIEW")));
            result.setConfidence(readConfidence(node));
            result.setRiskLevel(readText(node, "riskLevel", "MEDIUM"));
            result.setSummary(readText(node, "summary", ""));
            result.setExplanation(readText(node, "explanation", ""));
            result.setRecommendations(readRecommendations(node));
            return result;
        } catch (Exception ex) {
            log.warn("JSON OpenRouter invalide");
            throw new OpenRouterException(
                    OpenRouterErrorCode.OPENROUTER_INVALID_RESPONSE,
                    "Reponse JSON OpenRouter invalide",
                    0,
                    ex
            );
        }
    }

    public String toNormalizedJson(AIAnalysisResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception ex) {
            throw new OpenRouterException(
                    OpenRouterErrorCode.OPENROUTER_INVALID_RESPONSE,
                    "Erreur de serialisation de la reponse normalisee",
                    0,
                    ex
            );
        }
    }

    private String extractJsonPayload(String rawContent) {
        Matcher matcher = JSON_BLOCK.matcher(rawContent);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new OpenRouterException(
                OpenRouterErrorCode.OPENROUTER_INVALID_RESPONSE,
                "Aucun JSON detecte dans la reponse OpenRouter"
        );
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText(defaultValue) : defaultValue;
    }

    private double readConfidence(JsonNode node) {
        JsonNode value = node.get("confidence");
        if (value == null || value.isNull()) {
            return 0.0;
        }
        return value.asDouble(0.0);
    }

    private List<String> readRecommendations(JsonNode node) {
        JsonNode value = node.get("recommendations");
        if (value == null || !value.isArray()) {
            return List.of();
        }
        List<String> recommendations = new ArrayList<>();
        value.forEach(item -> recommendations.add(item.asText()));
        return recommendations;
    }

    private String normalizeDecision(String value) {
        if (value == null) {
            return "REVIEW";
        }
        return switch (value.trim().toUpperCase()) {
            case "APPROVE", "APPROUVER" -> "APPROUVER";
            case "REJECT", "REJETER" -> "REJETER";
            default -> "REVIEW";
        };
    }
}
