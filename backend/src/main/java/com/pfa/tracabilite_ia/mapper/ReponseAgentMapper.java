package com.pfa.tracabilite_ia.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.ReponseAgentResponse;
import com.pfa.tracabilite_ia.util.AgentStatusDisplayResolver;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentDisplayNames;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReponseAgentMapper {

    private final ObjectMapper objectMapper;

    public ReponseAgentMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ReponseAgentResponse toResponse(ReponseAgentIA entity) {
        boolean fallbackUsed = Boolean.TRUE.equals(entity.getFallbackUsed());
        String displayModelId = fallbackUsed && entity.getActualModelId() != null
                ? entity.getActualModelId()
                : entity.getModelId();
        String displayName = OpenRouterAgentDisplayNames.resolve(displayModelId);
        String fallbackMessage = fallbackUsed
                ? "Modèle principal indisponible — réponse produite par le modèle de secours"
                : null;

        return ReponseAgentResponse.builder()
                .reponseAgentId(entity.getReponseAgentId())
                .agentKey(entity.getAgentKey())
                .modelId(displayModelId)
                .displayName(displayName)
                .modelName(displayName)
                .provider(entity.getProvider())
                .decisionProposee(entity.getDecisionProposee())
                .declaredConfidence(entity.getConfianceDeclaree())
                .confianceDeclaree(entity.getConfianceDeclaree())
                .niveauRisque(entity.getNiveauRisque())
                .resume(entity.getResume())
                .explication(entity.getExplication())
                .recommandations(readRecommendations(entity.getRecommandationsJson()))
                .dureeMs(entity.getDureeMs())
                .nombreTokens(entity.getNombreTokens())
                .statut(entity.getStatut())
                .displayStatus(AgentStatusDisplayResolver.resolve(entity))
                .codeErreur(entity.getCodeErreur())
                .requestedModelId(entity.getRequestedModelId())
                .actualModelId(entity.getActualModelId())
                .fallbackUsed(entity.getFallbackUsed())
                .fallbackReason(entity.getFallbackReason())
                .responseHash(entity.getResponseHash())
                .retryCount(entity.getRetryCount())
                .fallbackMessage(fallbackMessage)
                .timestamp(entity.getTimestamp())
                .build();
    }

    public List<ReponseAgentResponse> toResponseList(List<ReponseAgentIA> entities) {
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private List<String> readRecommendations(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
