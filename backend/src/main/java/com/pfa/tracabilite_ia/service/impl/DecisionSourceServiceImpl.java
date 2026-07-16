package com.pfa.tracabilite_ia.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.request.CreateDecisionSourceRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionSourceResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.DecisionSource;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.DecisionSourceType;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.DecisionSourceRepository;
import com.pfa.tracabilite_ia.service.DecisionHashService;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import com.pfa.tracabilite_ia.service.DecisionSourceService;
import com.pfa.tracabilite_ia.util.HashUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DecisionSourceServiceImpl implements DecisionSourceService {

    private final DecisionSourceRepository decisionSourceRepository;
    private final DecisionRepository decisionRepository;
    private final DecisionHistoryService decisionHistoryService;
    private final DecisionHashService decisionHashService;
    private final ObjectMapper objectMapper;

    public DecisionSourceServiceImpl(DecisionSourceRepository decisionSourceRepository,
                                     DecisionRepository decisionRepository,
                                     DecisionHistoryService decisionHistoryService,
                                     DecisionHashService decisionHashService,
                                     ObjectMapper objectMapper) {
        this.decisionSourceRepository = decisionSourceRepository;
        this.decisionRepository = decisionRepository;
        this.decisionHistoryService = decisionHistoryService;
        this.decisionHashService = decisionHashService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void createDefaultSources(Decision decision, String featuresJson, String prompt,
                                     UUID createdById, String createdByEmail) {
        if (featuresJson != null && !featuresJson.isBlank()) {
            saveSource(decision, DecisionSourceType.BUSINESS_DATA,
                    "Données métier saisies",
                    "Données utilisées par le modèle LogisticRegression",
                    null, null, featuresJson, null,
                    createdById, createdByEmail, false);
        }
        if (prompt != null && !prompt.isBlank()) {
            saveSource(decision, DecisionSourceType.USER_INPUT,
                    "Prompt utilisateur",
                    "Description ou prompt saisi par l'utilisateur",
                    null, null, prompt, null,
                    createdById, createdByEmail, false);
        }
        refreshSourcesHash(decision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecisionSourceResponse> listByDecision(UUID decisionId) {
        decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));
        return decisionSourceRepository.findByDecisionDecisionIdOrderByCreatedAtAsc(decisionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DecisionSourceResponse addSource(UUID decisionId, CreateDecisionSourceRequest request,
                                            UUID createdById, String createdByEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));

        String canonical = canonicalSourceContent(request);
        DecisionSource saved = saveSource(decision, request.getSourceType(), request.getName(),
                request.getDescription(), request.getUrl(), request.getDocumentReference(),
                canonical, request.getMetadata(), createdById, createdByEmail, true);

        refreshSourcesHash(decision);
        decisionRepository.save(decision);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void removeSource(UUID decisionId, UUID sourceId, UUID performedById, String performedByEmail) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));
        DecisionSource source = decisionSourceRepository.findBySourceIdAndDecisionDecisionId(sourceId, decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Source introuvable : " + sourceId));

        decisionSourceRepository.delete(source);
        refreshSourcesHash(decision);
        decisionRepository.save(decision);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("sourceId", sourceId.toString());
        eventData.put("sourceName", source.getName());
        eventData.put("sourceType", source.getSourceType().name());

        decisionHistoryService.record(decision, DecisionHistoryAction.SOURCE_REMOVED,
                decision.getStatutValidation(), decision.getStatutValidation(),
                performedById, performedByEmail, null, null, eventData);
    }

    @Override
    @Transactional(readOnly = true)
    public String computeSourcesHash(UUID decisionId) {
        List<String> hashes = decisionSourceRepository.findByDecisionDecisionIdOrderByCreatedAtAsc(decisionId)
                .stream()
                .map(DecisionSource::getContentHash)
                .collect(Collectors.toList());
        return decisionHashService.computeSourcesHash(hashes);
    }

    @Override
    @Transactional
    public void refreshSourcesHash(Decision decision) {
        decision.setSourcesHash(computeSourcesHash(decision.getDecisionId()));
        decision.setCurrentHash(decision.calculerHash());
    }

    private DecisionSource saveSource(Decision decision,
                                      DecisionSourceType sourceType,
                                      String name,
                                      String description,
                                      String url,
                                      String documentReference,
                                      String contentForHash,
                                      Map<String, Object> metadata,
                                      UUID createdById,
                                      String createdByEmail,
                                      boolean recordHistory) {
        DecisionSource source = new DecisionSource();
        source.setDecision(decision);
        source.setSourceType(sourceType);
        source.setName(name);
        source.setDescription(description);
        source.setUrl(url);
        source.setDocumentReference(documentReference);
        source.setContentHash(HashUtils.sha256(contentForHash != null ? contentForHash.trim() : ""));
        source.setMetadataJson(writeJson(metadata));
        source.setCreatedById(createdById);
        source.setCreatedByEmail(createdByEmail);
        DecisionSource saved = decisionSourceRepository.save(source);

        if (recordHistory) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("sourceId", saved.getSourceId().toString());
            eventData.put("sourceType", sourceType.name());
            eventData.put("contentHash", saved.getContentHash());
            decisionHistoryService.record(decision, DecisionHistoryAction.SOURCE_ADDED,
                    decision.getStatutValidation(), decision.getStatutValidation(),
                    createdById, createdByEmail, null, null, eventData);
        }
        return saved;
    }

    private String canonicalSourceContent(CreateDecisionSourceRequest request) {
        return String.join("|",
                request.getSourceType().name(),
                request.getName(),
                request.getDescription() != null ? request.getDescription() : "",
                request.getUrl() != null ? request.getUrl() : "",
                request.getDocumentReference() != null ? request.getDocumentReference() : "");
    }

    private DecisionSourceResponse toResponse(DecisionSource source) {
        return DecisionSourceResponse.builder()
                .sourceId(source.getSourceId())
                .decisionId(source.getDecision().getDecisionId())
                .sourceType(source.getSourceType())
                .name(source.getName())
                .description(source.getDescription())
                .url(source.getUrl())
                .documentReference(source.getDocumentReference())
                .contentHash(source.getContentHash())
                .metadata(readMetadata(source.getMetadataJson()))
                .createdById(source.getCreatedById())
                .createdByEmail(source.getCreatedByEmail())
                .createdAt(source.getCreatedAt())
                .build();
    }

    private Map<String, Object> readMetadata(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private String writeJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            throw new IllegalStateException("Erreur serialisation metadata source", ex);
        }
    }
}
