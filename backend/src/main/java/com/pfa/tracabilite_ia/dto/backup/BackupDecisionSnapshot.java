package com.pfa.tracabilite_ia.dto.backup;

import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.DecisionSourceType;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupDecisionSnapshot {

    private UUID decisionId;
    private LocalDateTime timestamp;
    private String prompt;
    private String contexte;
    private String modelName;
    private String modelVersion;
    private String reponse;
    private StatutDecisionEnum statutValidation;
    private String previousHash;
    private String currentHash;
    private String featuresJson;
    private String suggestedDecision;
    private Double confidenceScore;
    private String riskLevel;
    private String probabilitiesJson;
    private String resumeConsensus;
    private String consensusJson;
    private String explanationSource;
    private String businessDataHash;
    private String sourcesHash;
    private String agentResponsesHash;
    private String humanDecision;
    private String validatorEmail;
    private DecisionDomain domaine;
    private String dossierReference;
    private String description;
    private String datasetVersion;
    private String sourceDonnees;
    private Boolean accordAvecIa;
    private String justificationHumaine;
    private UUID validateurId;
    private String validateurRole;
    private LocalDateTime submittedAt;
    private LocalDateTime validatedAt;
    private String createdBy;
    private LocalDateTime updatedAt;

    private BackupCreditSnapshot creditData;
    private BackupMedicalSnapshot medicalData;
    private BackupEducationSnapshot educationData;

    @Builder.Default
    private List<BackupSourceSnapshot> sources = new ArrayList<>();

    @Builder.Default
    private List<BackupAgentSnapshot> agentResponses = new ArrayList<>();

    @Builder.Default
    private List<BackupFactorSnapshot> explanationFactors = new ArrayList<>();

    @Builder.Default
    private List<BackupHistorySnapshot> history = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupCreditSnapshot {
        private Integer age;
        private Integer dureeMois;
        private String typeContrat;
        private String statutLogement;
        private Integer incidentPaiementBam;
        private Double montantDemandeMad;
        private Double nouvelleEcheanceMad;
        private Double revenuMensuelMad;
        private Double tauxEndettement;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupMedicalSnapshot {
        private Integer age;
        private Integer grossesses;
        private Double glycemieMgDl;
        private Double pressionArterielleMmhg;
        private Double epaisseurPliCutaneMm;
        private Double insulineMicroUMl;
        private Double imcKgM2;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupEducationSnapshot {
        private Integer ageInscription;
        private Double noteAdmission;
        private Double noteQualificationPrecedente;
        private Integer unitesValideesS1;
        private Double moyenneS1;
        private Integer unitesValideesS2;
        private Double moyenneS2;
        private Double tauxChomage;
        private Double tauxInflation;
        private Double pib;
        private String sexe;
        private String boursier;
        private String fraisAJour;
        private String debiteur;
        private String deplace;
        private String international;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupSourceSnapshot {
        private DecisionSourceType sourceType;
        private String name;
        private String description;
        private String url;
        private String documentReference;
        private String contentHash;
        private String metadataJson;
        private UUID createdById;
        private String createdByEmail;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupAgentSnapshot {
        private String agentKey;
        private String modelId;
        private String modelName;
        private String provider;
        private String requestedModelId;
        private String actualModelId;
        private Boolean fallbackUsed;
        private String fallbackReason;
        private String responseHash;
        private Integer retryCount;
        private String reponseBrute;
        private String reponseNormalisee;
        private String decisionProposee;
        private Double confianceDeclaree;
        private String niveauRisque;
        private String resume;
        private String explication;
        private String recommandationsJson;
        private Long dureeMs;
        private Integer nombreTokens;
        private StatutReponseAgentEnum statut;
        private String codeErreur;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupFactorSnapshot {
        private String name;
        private String value;
        private Double shapValue;
        private String impact;
        private Integer rank;
        private Double contributionPercent;
        private String source;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BackupHistorySnapshot {
        private DecisionHistoryAction action;
        private StatutDecisionEnum previousStatus;
        private StatutDecisionEnum newStatus;
        private UUID performedById;
        private String performedByEmail;
        private String comment;
        private String justification;
        private String eventDataJson;
        private String correlationId;
        private LocalDateTime createdAt;
    }
}
