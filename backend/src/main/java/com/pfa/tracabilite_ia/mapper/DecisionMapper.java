package com.pfa.tracabilite_ia.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfa.tracabilite_ia.dto.response.*;
import com.pfa.tracabilite_ia.entities.*;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DecisionMapper {

    private final ObjectMapper objectMapper;
    private final ReponseAgentMapper reponseAgentMapper;

    public DecisionMapper(ObjectMapper objectMapper, ReponseAgentMapper reponseAgentMapper) {
        this.objectMapper = objectMapper;
        this.reponseAgentMapper = reponseAgentMapper;
    }

    public DecisionResponse toResponse(Decision decision) {
        ConsensusResponse consensus = readConsensus(decision.getConsensusJson());
        DecisionDomain domain = decision.getDomaine() != null ? decision.getDomaine() : DecisionDomain.CREDIT;

        DecisionResponse.DecisionResponseBuilder builder = DecisionResponse.builder()
                .decisionId(decision.getDecisionId())
                .reference(formatReference(decision.getDecisionId()))
                .domaine(domain.name())
                .dossierReference(decision.getDossierReference())
                .description(decision.getDescription())
                .datasetVersion(decision.getDatasetVersion())
                .sourceDonnees(decision.getSourceDonnees())
                .accordAvecIa(decision.getAccordAvecIa())
                .justificationHumaine(maskMedicalJustification(domain, decision.getJustificationHumaine()))
                .validateurRole(decision.getValidateurRole())
                .validateurId(decision.getValidateurId())
                .createdBy(decision.getCreatedBy())
                .prompt(decision.getPrompt())
                .contexte(decision.getContexte())
                .modelName(decision.getModelName())
                .modelVersion(decision.getModelVersion())
                .reponse(decision.getReponse())
                .statutValidation(decision.getStatutValidation())
                .suggestedDecision(decision.getSuggestedDecision())
                .confidenceScore(decision.getConfidenceScore())
                .riskLevel(decision.getRiskLevel())
                .explanationSource(decision.getExplanationSource())
                .resumeConsensus(decision.getResumeConsensus())
                .consensus(consensus)
                .consensusDecision(consensus != null ? consensus.getDecisionConsensus() : null)
                .mlPrediction(buildMlPrediction(decision))
                .agentResponses(reponseAgentMapper.toResponseList(decision.getReponsesAgents()))
                .features(readFeatures(decision.getFeaturesJson()))
                .probabilities(readProbabilities(decision.getProbabilitiesJson()))
                .factors(mapFactors(decision.getExplanationFactors()))
                .humanFinalDecision(decision.getHumanDecision())
                .validatorEmail(decision.getValidatorEmail())
                .validatedAt(decision.getValidatedAt())
                .submittedAt(decision.getSubmittedAt())
                .timestamp(decision.getTimestamp())
                .currentHash(decision.getCurrentHash())
                .integrity(buildIntegrity(decision))
                .sourcesMeta(buildSourcesMeta(decision, domain));

        attachDomainData(builder, decision, domain);
        return builder.build();
    }

    public void applyDomainData(
            DecisionResponse response,
            CreditDecisionData credit,
            MedicalDecisionData medical,
            EducationDecisionData education
    ) {
        DecisionDomain domain = response.getDomaine() != null
                ? DecisionDomain.valueOf(response.getDomaine())
                : DecisionDomain.CREDIT;
        if (domain == DecisionDomain.CREDIT && credit != null) {
            response.setCreditData(mapCredit(credit));
            response.setMedicalData(null);
            response.setEducationData(null);
        } else if (domain == DecisionDomain.MEDICAL && medical != null) {
            response.setMedicalData(mapMedical(medical));
            response.setCreditData(null);
            response.setEducationData(null);
        } else if (domain == DecisionDomain.EDUCATION && education != null) {
            response.setEducationData(mapEducation(education));
            response.setCreditData(null);
            response.setMedicalData(null);
        }
    }

    private void attachDomainData(
            DecisionResponse.DecisionResponseBuilder builder,
            Decision decision,
            DecisionDomain domain
    ) {
        // Prefer already-initialized associations when present (avoid LazyInitializationException).
        try {
            if (domain == DecisionDomain.CREDIT && decision.getCreditData() != null) {
                builder.creditData(mapCredit(decision.getCreditData()));
            } else if (domain == DecisionDomain.MEDICAL && decision.getMedicalData() != null) {
                builder.medicalData(mapMedical(decision.getMedicalData()));
            } else if (domain == DecisionDomain.EDUCATION && decision.getEducationData() != null) {
                builder.educationData(mapEducation(decision.getEducationData()));
            }
        } catch (org.hibernate.LazyInitializationException ignored) {
            // Loaded later via applyDomainData in DecisionServiceImpl
        }
    }

    public CreditDecisionDataResponse mapCredit(CreditDecisionData d) {
        return CreditDecisionDataResponse.builder()
                .age(d.getAge())
                .dureeMois(d.getDureeMois())
                .typeContrat(d.getTypeContrat())
                .statutLogement(d.getStatutLogement())
                .incidentPaiementBam(d.getIncidentPaiementBam())
                .montantDemandeMad(d.getMontantDemandeMad())
                .nouvelleEcheanceMad(d.getNouvelleEcheanceMad())
                .revenuMensuelMad(d.getRevenuMensuelMad())
                .tauxEndettement(d.getTauxEndettement())
                .build();
    }

    public MedicalDecisionDataResponse mapMedical(MedicalDecisionData d) {
        return MedicalDecisionDataResponse.builder()
                .age(d.getAge())
                .grossesses(d.getGrossesses())
                .glycemieMgDl(d.getGlycemieMgDl())
                .pressionArterielleMmhg(d.getPressionArterielleMmhg())
                .epaisseurPliCutaneMm(d.getEpaisseurPliCutaneMm())
                .insulineMicroUMl(d.getInsulineMicroUMl())
                .imcKgM2(d.getImcKgM2())
                .build();
    }

    public EducationDecisionDataResponse mapEducation(EducationDecisionData d) {
        return EducationDecisionDataResponse.builder()
                .ageInscription(d.getAgeInscription())
                .noteAdmission(d.getNoteAdmission())
                .noteQualificationPrecedente(d.getNoteQualificationPrecedente())
                .unitesValideesS1(d.getUnitesValideesS1())
                .moyenneS1(d.getMoyenneS1())
                .unitesValideesS2(d.getUnitesValideesS2())
                .moyenneS2(d.getMoyenneS2())
                .tauxChomage(d.getTauxChomage())
                .tauxInflation(d.getTauxInflation())
                .pib(d.getPib())
                .sexe(d.getSexe())
                .boursier(d.getBoursier())
                .fraisAJour(d.getFraisAJour())
                .debiteur(d.getDebiteur())
                .deplace(d.getDeplace())
                .international(d.getInternational())
                .build();
    }

    public void applyValidationMetadata(DecisionResponse response, List<ValidationActionResponse> validations) {
        response.setValidations(validations);
        if (validations == null || validations.isEmpty()) {
            return;
        }
        ValidationActionResponse latest = validations.get(0);
        if (response.getValidatedAt() == null) {
            response.setValidatedAt(latest.getTimestamp());
        }
        response.setHumanFinalAction(latest.getTypeAction());
        if (latest.getTypeAction() == TypeActionEnum.MODIFIER) {
            response.setHumanFinalDecision(latest.getDecisionHumaine());
        } else if (latest.getTypeAction() == TypeActionEnum.REVIEW) {
            response.setHumanFinalDecision("REVIEW");
        } else if (response.getHumanFinalDecision() == null) {
            response.setHumanFinalDecision(latest.getDecisionHumaine());
        }
    }

    public List<DecisionResponse> toResponseList(List<Decision> decisions) {
        return decisions.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String maskMedicalJustification(DecisionDomain domain, String justification) {
        if (domain == DecisionDomain.MEDICAL && justification != null && !justification.isBlank()) {
            // Detail view for authorized roles still shows justification; export masks separately.
            return justification;
        }
        return justification;
    }

    private DecisionIntegrityView buildIntegrity(Decision decision) {
        return DecisionIntegrityView.builder()
                .currentHash(decision.getCurrentHash())
                .previousHash(decision.getPreviousHash())
                .businessDataHash(decision.getBusinessDataHash())
                .sourcesHash(decision.getSourcesHash())
                .agentResponsesHash(decision.getAgentResponsesHash())
                .explanation("Le hash SHA-256 permet de vérifier l'intégrité du snapshot de la décision. "
                        + "Il ne bloque pas directement les modifications.")
                .build();
    }

    private DecisionSourcesMetaView buildSourcesMeta(Decision decision, DecisionDomain domain) {
        return switch (domain) {
            case CREDIT -> DecisionSourcesMetaView.builder()
                    .sourceDonnees(nvl(decision.getSourceDonnees(), "credit-maroc-synthetic"))
                    .datasetVersion(nvl(decision.getDatasetVersion(), "credit-maroc-synthetic-v1.0.0"))
                    .modelVersion(decision.getModelVersion())
                    .modelName(decision.getModelName())
                    .pipelineName("credit_pipeline")
                    .featureCount(9)
                    .dataType("Données synthétiques contextualisées au Maroc")
                    .synthetic(true)
                    .disclaimer("Dataset synthétique — pas un modèle bancaire officiel.")
                    .usageLimit("Démonstration du risque de défaut uniquement.")
                    .build();
            case MEDICAL -> DecisionSourcesMetaView.builder()
                    .sourceDonnees(nvl(decision.getSourceDonnees(), "medical-diabetes-maroc-synthetic"))
                    .datasetVersion(nvl(decision.getDatasetVersion(), "medical-diabetes-maroc-synthetic-v1.0.0"))
                    .modelVersion(decision.getModelVersion())
                    .modelName(decision.getModelName())
                    .pipelineName("medical_pipeline")
                    .featureCount(7)
                    .dataType("Données synthétiques contextualisées au Maroc")
                    .synthetic(true)
                    .disclaimer("Estimation indicative uniquement. Ne remplace pas un diagnostic médical.")
                    .usageLimit("Aide à l'évaluation du risque — pas un diagnostic.")
                    .build();
            case EDUCATION -> DecisionSourcesMetaView.builder()
                    .sourceDonnees(nvl(decision.getSourceDonnees(), "students-maroc-dropout-synthetic"))
                    .datasetVersion(nvl(decision.getDatasetVersion(), "students-maroc-dropout-synthetic-v1.0.0"))
                    .modelVersion(decision.getModelVersion())
                    .modelName(decision.getModelName())
                    .pipelineName("education_pipeline")
                    .featureCount(16)
                    .dataType("Données synthétiques contextualisées au Maroc")
                    .synthetic(true)
                    .disclaimer("Aide à l'accompagnement pédagogique. Pas une sanction automatique.")
                    .usageLimit("Accompagnement pédagogique uniquement.")
                    .build();
        };
    }

    private static String nvl(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private MlPredictionView buildMlPrediction(Decision decision) {
        return MlPredictionView.builder()
                .decision(decision.getSuggestedDecision())
                .confidenceScore(decision.getConfidenceScore())
                .riskLevel(decision.getRiskLevel())
                .modelName(decision.getModelName())
                .modelVersion(decision.getModelVersion())
                .build();
    }

    private String formatReference(java.util.UUID decisionId) {
        if (decisionId == null) {
            return null;
        }
        return decisionId.toString().substring(0, 8).toUpperCase();
    }

    private ConsensusResponse readConsensus(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ConsensusResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> readFeatures(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private Map<String, Double> readProbabilities(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private List<DecisionResponse.ExplanationFactorResponse> mapFactors(List<ExplanationFactor> factors) {
        if (factors == null) {
            return Collections.emptyList();
        }
        return factors.stream()
                .sorted((left, right) -> Integer.compare(
                        left.getRank() != null ? left.getRank() : 99,
                        right.getRank() != null ? right.getRank() : 99))
                .map(factor -> DecisionResponse.ExplanationFactorResponse.builder()
                        .factorId(factor.getFactorId())
                        .name(factor.getName())
                        .value(factor.getValue())
                        .shapValue(factor.getShapValue())
                        .impact(factor.getImpact())
                        .rank(factor.getRank())
                        .contributionPercent(factor.getContributionPercent())
                        .source(factor.getSource())
                        .build())
                .collect(Collectors.toList());
    }
}
