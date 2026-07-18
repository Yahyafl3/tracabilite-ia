package com.pfa.tracabilite_ia.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.AIAnalysisResult;
import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import com.pfa.tracabilite_ia.util.AgentTextSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    public ParsedAgentResponse parse(String rawContent) {
        String jsonPayload = extractJsonPayload(rawContent);
        String parseablePayload = sanitizeJsonPayloadForParsing(jsonPayload);
        try {
            JsonNode node = objectMapper.readTree(parseablePayload);
            AIAnalysisResult result = new AIAnalysisResult();

            String rawDecision = readOptionalText(node, "suggestedDecision");
            String normalizedDecision = normalizeDecision(rawDecision);
            result.setSuggestedDecision(normalizedDecision);

            result.setConfidence(readDeclaredConfidence(node));
            result.setRiskLevel(normalizeRiskLevel(readOptionalText(node, "riskLevel")));
            result.setSummary(AgentTextSanitizer.sanitize(readOptionalText(node, "summary")));
            result.setExplanation(AgentTextSanitizer.sanitize(readOptionalText(node, "explanation")));
            result.setRecommendations(readRecommendations(node));

            boolean valid = isValid(result, rawDecision);
            if (!valid) {
                log.warn("Réponse agent invalide après validation");
            }
            return new ParsedAgentResponse(valid, result, toNormalizedJson(result));
        } catch (OpenRouterException ex) {
            throw ex;
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

    private boolean isValid(AIAnalysisResult result, String rawDecision) {
        if (result.getSuggestedDecision() == null) {
            return false;
        }
        if (rawDecision != null && normalizeDecision(rawDecision) == null) {
            return false;
        }
        if (isBlank(result.getSummary()) && isBlank(result.getExplanation())) {
            return false;
        }
        return true;
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

    private String readOptionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private Double readDeclaredConfidence(JsonNode node) {
        JsonNode value = node.get("confidence");
        if (value == null || value.isNull() || !value.isNumber()) {
            return null;
        }
        double confidence = value.doubleValue();
        if (confidence < 0.0 || confidence > 1.0) {
            return null;
        }
        return confidence;
    }

    private List<String> readRecommendations(JsonNode node) {
        JsonNode value = node.get("recommendations");
        if (value == null || !value.isArray()) {
            return List.of();
        }
        List<String> recommendations = new ArrayList<>();
        value.forEach(item -> {
            String sanitized = AgentTextSanitizer.sanitize(item.asText());
            if (sanitized != null && !sanitized.isBlank()) {
                recommendations.add(sanitized);
            }
        });
        return recommendations;
    }

    private String normalizeDecision(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "APPROVE", "APPROUVER" -> "APPROUVER";
            case "REJECT", "REJETER" -> "REJETER";
            case "REVIEW" -> "REVIEW";
            default -> null;
        };
    }

    private String normalizeRiskLevel(String value) {
        if (value == null || value.isBlank()) {
            return "MEDIUM";
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "LOW", "MEDIUM", "HIGH" -> value.trim().toUpperCase(Locale.ROOT);
            default -> "MEDIUM";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sanitizeJsonPayloadForParsing(String jsonPayload) {
        StringBuilder builder = new StringBuilder(jsonPayload.length());
        for (int index = 0; index < jsonPayload.length(); index++) {
            char character = jsonPayload.charAt(index);
            if (character == '\n' || character == '\r' || character == '\t' || !Character.isISOControl(character)) {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
