package com.pfa.tracabilite_ia.service.impl;



import com.fasterxml.jackson.databind.ObjectMapper;

import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;

import com.pfa.tracabilite_ia.dto.request.DecisionRequest;

import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;

import com.pfa.tracabilite_ia.dto.response.DecisionResponse;

import com.pfa.tracabilite_ia.dto.response.MLPredictionResponse;

import com.pfa.tracabilite_ia.entities.Decision;

import com.pfa.tracabilite_ia.entities.ExplanationFactor;

import com.pfa.tracabilite_ia.entities.SystemeIA;

import com.pfa.tracabilite_ia.entities.Utilisateur;

import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;

import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;

import com.pfa.tracabilite_ia.mapper.DecisionMapper;

import com.pfa.tracabilite_ia.mapper.ValidationMapper;

import com.pfa.tracabilite_ia.groq.GroqMultiAgentService;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRetryService;

import com.pfa.tracabilite_ia.repository.DecisionRepository;

import com.pfa.tracabilite_ia.repository.SystemeIARepository;

import com.pfa.tracabilite_ia.repository.ValidationActionRepository;

import com.pfa.tracabilite_ia.service.*;

import com.pfa.tracabilite_ia.validation.CreditFeaturesValidator;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.HashMap;

import java.util.Map;

import java.util.UUID;



@Service

public class DecisionServiceImpl implements DecisionService {



    private static final Logger log = LoggerFactory.getLogger(DecisionServiceImpl.class);



    private final DecisionRepository decisionRepository;

    private final SystemeIARepository systemeIARepository;

    private final ValidationActionRepository validationActionRepository;

    private final MLDecisionService mlDecisionService;

    private final GroqMultiAgentService groqMultiAgentService;

    private final OpenRouterAgentRetryService openRouterAgentRetryService;

    private final DecisionMapper decisionMapper;

    private final ValidationMapper validationMapper;

    private final ObjectMapper objectMapper;

    private final CreditFeaturesValidator creditFeaturesValidator;

    private final DecisionHistoryService decisionHistoryService;

    private final DecisionSourceService decisionSourceService;

    private final DecisionHashService decisionHashService;

    private final AuthService authService;

    private final DecisionScopeService decisionScopeService;



    public DecisionServiceImpl(DecisionRepository decisionRepository,

                                 SystemeIARepository systemeIARepository,

                                 ValidationActionRepository validationActionRepository,

                                 MLDecisionService mlDecisionService,

                                 GroqMultiAgentService groqMultiAgentService,

                                 OpenRouterAgentRetryService openRouterAgentRetryService,

                                 DecisionMapper decisionMapper,

                                 ValidationMapper validationMapper,

                                 ObjectMapper objectMapper,

                                 CreditFeaturesValidator creditFeaturesValidator,

                                 DecisionHistoryService decisionHistoryService,

                                 DecisionSourceService decisionSourceService,

                                 DecisionHashService decisionHashService,

                                 AuthService authService,

                                 DecisionScopeService decisionScopeService) {

        this.decisionRepository = decisionRepository;

        this.systemeIARepository = systemeIARepository;

        this.validationActionRepository = validationActionRepository;

        this.mlDecisionService = mlDecisionService;

        this.groqMultiAgentService = groqMultiAgentService;

        this.openRouterAgentRetryService = openRouterAgentRetryService;

        this.decisionMapper = decisionMapper;

        this.validationMapper = validationMapper;

        this.objectMapper = objectMapper;

        this.creditFeaturesValidator = creditFeaturesValidator;

        this.decisionHistoryService = decisionHistoryService;

        this.decisionSourceService = decisionSourceService;

        this.decisionHashService = decisionHashService;

        this.authService = authService;

        this.decisionScopeService = decisionScopeService;

    }



    @Override

    @Transactional

    public Decision creer(DecisionRequest request) {

        Decision decision = mapToEntity(request, new Decision());

        if (decision.getStatutValidation() == null) {

            decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE);

        }

        Decision saved = decisionRepository.save(decision);

        recordHistory(saved, DecisionHistoryAction.DECISION_CREATED, null, saved.getStatutValidation(), null);

        refreshHash(saved);

        return decisionRepository.save(saved);

    }



    @Override

    @Transactional(readOnly = true)
    public DecisionResponse obtenir(UUID id) {

        Decision decision = decisionScopeService.loadForRead(id);

        DecisionResponse response = decisionMapper.toResponse(decision);

        decisionMapper.applyValidationMetadata(response, validationMapper.toResponseList(

                validationActionRepository.findByDecisionDecisionIdOrderByTimestampDesc(id)));

        return response;

    }



    @Override

    @Transactional

    public DecisionResponse mettreAJour(UUID id, DecisionRequest request) {

        Decision decision = decisionRepository.findById(id)

                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + id));

        mapToEntity(request, decision);

        refreshHash(decision);

        return decisionMapper.toResponse(decisionRepository.save(decision));

    }



    @Override

    @Transactional(readOnly = true)

    public DecisionPageResponse rechercher(String search, StatutDecisionEnum statut, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Decision> result = decisionRepository.search(search, statut, pageable);

        return DecisionPageResponse.builder()

                .content(decisionMapper.toResponseList(result.getContent()))

                .page(result.getNumber())

                .size(result.getSize())

                .totalElements(result.getTotalElements())

                .totalPages(result.getTotalPages())

                .last(result.isLast())

                .build();

    }



    @Override

    @Transactional

    public DecisionResponse analyserCredit(CreditFeaturesRequest request) {

        creditFeaturesValidator.validate(request);

        Utilisateur user = authService.getCurrentUser();



        Decision decision = new Decision();

        decision.setStatutValidation(StatutDecisionEnum.BROUILLON);

        decision.setPrompt(request.getDescription() != null && !request.getDescription().isBlank()

                ? request.getDescription()

                : "Analyse de demande de crédit");

        decision.setModelName("ML-Credit");

        decision.setModelVersion("1.0.0");

        decision.setReponse("Analyse en cours...");

        decision.setContexte(buildContexte(request));

        decision.setFeaturesJson(writeJson(buildFeaturesMap(request)));

        decisionRepository.save(decision);



        recordHistory(decision, DecisionHistoryAction.DECISION_CREATED, null, StatutDecisionEnum.BROUILLON, user);

        recordHistory(decision, DecisionHistoryAction.ML_ANALYSIS_STARTED, StatutDecisionEnum.BROUILLON,

                StatutDecisionEnum.BROUILLON, user);



        try {

            MLPredictionResponse prediction = mlDecisionService.predict(request);

            applyPrediction(decision, prediction, request);

            decision.setBusinessDataHash(decisionHashService.computeBusinessDataHash(decision.getFeaturesJson()));

            decisionSourceService.createDefaultSources(decision, decision.getFeaturesJson(), decision.getPrompt(),

                    user.getId(), user.getEmail());



            Map<String, Object> mlEvent = new HashMap<>();

            mlEvent.put("decision", prediction.getDecision());

            mlEvent.put("confidence", prediction.getScoreConfiance());

            mlEvent.put("riskLevel", prediction.getRiskLevel());

            recordHistory(decision, DecisionHistoryAction.ML_ANALYSIS_COMPLETED, StatutDecisionEnum.BROUILLON,

                    StatutDecisionEnum.BROUILLON, user, mlEvent);

        } catch (Exception ex) {

            recordHistory(decision, DecisionHistoryAction.ML_ANALYSIS_FAILED, StatutDecisionEnum.BROUILLON,

                    StatutDecisionEnum.BROUILLON, user, Map.of("error", ex.getMessage()));

            throw ex;

        }



        if (request.isIncludeOpenRouter()) {

            enrichWithOpenRouter(decision, request, user);

        }



        StatutDecisionEnum previous = decision.getStatutValidation();

        decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE);

        recordHistory(decision, DecisionHistoryAction.DECISION_SUBMITTED_FOR_VALIDATION,

                previous, StatutDecisionEnum.EN_ATTENTE, user);



        decisionHashService.refreshHashComponents(decision, decision.getReponsesAgents());

        decisionSourceService.refreshSourcesHash(decision);

        chainWithPrevious(decision);

        Decision saved = decisionRepository.save(decision);

        log.info("Decision {} analysee: {} ({})", saved.getDecisionId(), saved.getSuggestedDecision(), saved.getRiskLevel());

        return decisionMapper.toResponse(saved);

    }



    private void enrichWithOpenRouter(Decision decision, CreditFeaturesRequest request, Utilisateur user) {

        try {

            MLPredictionResponse prediction = new MLPredictionResponse();

            prediction.setDecision(decision.getSuggestedDecision());

            prediction.setScoreConfiance(decision.getConfidenceScore());

            prediction.setRiskLevel(decision.getRiskLevel());



            var bundle = groqMultiAgentService.analyzeDecisionAgents(

                    decision,

                    buildOpenRouterPrompt(request, prediction),

                    """
                    Analyse complementaire Groq uniquement. Ne pas inventer de poids SHAP ni remplacer la prediction ML.
                    La confiance ML fournie est une donnee de contexte. Ne la recopiez pas automatiquement comme votre propre confiance.
                    Retournez votre propre niveau de confiance uniquement si vous pouvez le justifier.
                    """,

                    user

            );

            decision.setAgentResponsesHash(decisionHashService.computeAgentResponsesHash(decision.getReponsesAgents()));

        } catch (Exception ex) {

            log.warn("Groq indisponible pour la decision {}: {}", decision.getDecisionId(), ex.getMessage());

            decision.setResumeConsensus(null);

            decision.setConsensusJson(null);

        }

    }



    private void applyPrediction(Decision decision, MLPredictionResponse prediction, CreditFeaturesRequest request) {

        decision.setSuggestedDecision(prediction.getDecision());

        decision.setConfidenceScore(prediction.getScoreConfiance());

        decision.setRiskLevel(prediction.getRiskLevel());

        decision.setExplanationSource(prediction.getExplanationSource());

        decision.setProbabilitiesJson(writeJson(prediction.getProbabilities()));

        decision.setFeaturesJson(writeJson(prediction.getFeatures() != null

                ? prediction.getFeatures()

                : buildFeaturesMap(request)));



        if (prediction.getModel() != null) {

            Object version = prediction.getModel().get("version");

            Object name = prediction.getModel().get("name");

            if (version != null) {

                decision.setModelVersion(version.toString());

            }

            if (name != null) {

                decision.setModelName(name.toString());

            }

        }



        decision.getExplanationFactors().clear();

        if (prediction.getFactors() != null) {

            for (MLPredictionResponse.DecisionFactor factor : prediction.getFactors()) {

                ExplanationFactor entity = new ExplanationFactor();

                entity.setDecision(decision);

                entity.setName(factor.getName());

                entity.setValue(String.valueOf(factor.getValue()));

                entity.setShapValue(factor.getShapValue());

                entity.setImpact(factor.getImpact());

                entity.setRank(factor.getRank());

                entity.setContributionPercent(factor.getContributionPercent());

                entity.setSource(prediction.getExplanationSource());

                decision.getExplanationFactors().add(entity);

            }

        }



        decision.setReponse(buildMlReponse(prediction));

    }



    private void chainWithPrevious(Decision decision) {

        decisionRepository.findTopByDecisionIdNotOrderByTimestampDesc(decision.getDecisionId())

                .ifPresentOrElse(

                        decision::chainerAvecPrecedent,

                        () -> {

                            decision.setPreviousHash(null);

                            decision.setCurrentHash(decision.calculerHash());

                        }

                );

    }



    private void refreshHash(Decision decision) {

        decision.setBusinessDataHash(decisionHashService.computeBusinessDataHash(decision.getFeaturesJson()));

        decision.setAgentResponsesHash(decisionHashService.computeAgentResponsesHash(decision.getReponsesAgents()));

        decisionSourceService.refreshSourcesHash(decision);

    }



    private void recordHistory(Decision decision, DecisionHistoryAction action,

                               StatutDecisionEnum previous, StatutDecisionEnum next, Utilisateur user) {

        recordHistory(decision, action, previous, next, user, null);

    }



    private void recordHistory(Decision decision, DecisionHistoryAction action,

                               StatutDecisionEnum previous, StatutDecisionEnum next,

                               Utilisateur user, Map<String, Object> eventData) {

        decisionHistoryService.record(decision, action, previous, next,

                user != null ? user.getId() : null,

                user != null ? user.getEmail() : null,

                null, null, eventData);

    }



    private String buildOpenRouterPrompt(CreditFeaturesRequest request, MLPredictionResponse prediction) {

        return """

                Analyse cette demande de credit et produis un resume textuel clair pour un validateur humain.

                Decision ML: %s

                Confiance: %s%%

                Risque: %s

                Montant: %s

                Revenu mensuel: %s

                Secteur: %s

                Ratio d'endettement: %s

                Incidents de paiement: %s

                """.formatted(

                prediction.getDecision(),

                prediction.getScoreConfiance(),

                prediction.getRiskLevel(),

                request.getAmount(),

                request.getMonthlyIncome(),

                request.getSector(),

                request.getDebtRatio(),

                request.getPaymentIncidents());

    }



    private String buildMlReponse(MLPredictionResponse prediction) {

        return "Decision ML: " + prediction.getDecision()

                + " | Confiance: " + prediction.getScoreConfiance() + "%"

                + " | Risque: " + prediction.getRiskLevel()

                + " | Source explicabilite: " + prediction.getExplanationSource();

    }



    private Map<String, Object> buildFeaturesMap(CreditFeaturesRequest request) {

        Map<String, Object> features = new HashMap<>();

        features.put("amount", request.getAmount());

        features.put("monthlyIncome", request.getMonthlyIncome());

        features.put("companyAgeYears", request.getCompanyAgeYears());

        features.put("paymentIncidents", request.getPaymentIncidents());

        features.put("debtRatio", request.getDebtRatio());

        features.put("sector", request.getSector().name());

        return features;

    }



    private String buildContexte(CreditFeaturesRequest request) {

        return "Credit | montant=" + request.getAmount()

                + " | revenu=" + request.getMonthlyIncome()

                + " | secteur=" + request.getSector().name()

                + " | debtRatio=" + request.getDebtRatio();

    }



    private String writeJson(Object value) {

        try {

            return objectMapper.writeValueAsString(value);

        } catch (Exception ex) {

            throw new IllegalStateException("Erreur de serialisation JSON", ex);

        }

    }



    private Decision mapToEntity(DecisionRequest request, Decision decision) {

        decision.setPrompt(request.getPrompt());

        decision.setContexte(request.getContexte());

        decision.setModelName(request.getModelName());

        decision.setModelVersion(request.getModelVersion());

        decision.setReponse(request.getReponse());



        if (request.getSystemeIaId() != null) {

            SystemeIA systemeIA = systemeIARepository.findById(request.getSystemeIaId())

                    .orElseThrow(() -> new ResourceNotFoundException(

                            "Systeme IA introuvable : " + request.getSystemeIaId()));

            decision.setSystemeIa(systemeIA);

        }



        if (request.getStatutValidation() != null) {

            decision.setStatutValidation(request.getStatutValidation());

        }



        return decision;

    }

    @Override
    @Transactional
    public DecisionResponse retryFailedAgents(UUID id, Utilisateur user) {
        return openRouterAgentRetryService.retryFailedAgents(id, user);
    }

}

