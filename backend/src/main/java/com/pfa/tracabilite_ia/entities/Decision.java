package com.pfa.tracabilite_ia.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.util.HashUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "decision")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID decisionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String contexte;

    @Column(nullable = false)
    private String modelName;

    private String modelVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "systeme_ia_id")
    private SystemeIA systemeIa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDecisionEnum statutValidation = StatutDecisionEnum.EN_ATTENTE;

    @Column(length = 64)
    private String previousHash;

    @Column(length = 64)
    private String currentHash;

    @Column(columnDefinition = "TEXT")
    private String featuresJson;

    private String suggestedDecision;

    private Double confidenceScore;

    private String riskLevel;

    @Column(columnDefinition = "TEXT")
    private String probabilitiesJson;

    @Column(name = "resume_ollama", columnDefinition = "TEXT")
    private String resumeConsensus;

    @Column(columnDefinition = "TEXT")
    private String consensusJson;

    private String explanationSource;

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ExplanationFactor> explanationFactors = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ReponseAgentIA> reponsesAgents = new java.util.ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_precedente_id")
    private Decision decisionPrecedente;

    @Column(length = 64)
    private String businessDataHash;

    @Column(length = 64)
    private String sourcesHash;

    @Column(length = 64)
    private String agentResponsesHash;

    @Column(length = 32)
    private String humanDecision;

    @Column(length = 255)
    private String validatorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "domaine", length = 32)
    private DecisionDomain domaine = DecisionDomain.CREDIT;

    @Column(length = 64)
    private String dossierReference;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 128)
    private String datasetVersion;

    @Column(length = 128)
    private String sourceDonnees;

    private Boolean accordAvecIa;

    @Column(columnDefinition = "TEXT")
    private String justificationHumaine;

    private UUID validateurId;

    @Column(length = 64)
    private String validateurRole;

    private LocalDateTime submittedAt;

    private LocalDateTime validatedAt;

    @Column(length = 255)
    private String createdBy;

    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CreditDecisionData creditData;

    @OneToOne(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MedicalDecisionData medicalData;

    @OneToOne(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EducationDecisionData educationData;

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<DecisionSource> sources = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<DecisionHistory> historyEntries = new java.util.ArrayList<>();

    public String calculerHash() {
        String payload = String.join("|",
                decisionId != null ? decisionId.toString() : "",
                domaine != null ? domaine.name() : "",
                decisionId != null ? decisionId.toString() : "",
                prompt != null ? prompt : "",
                contexte != null ? contexte : "",
                businessDataHash != null ? businessDataHash : "",
                sourcesHash != null ? sourcesHash : "",
                suggestedDecision != null ? suggestedDecision : "",
                confidenceScore != null ? confidenceScore.toString() : "",
                modelName != null ? modelName : "",
                modelVersion != null ? modelVersion : "",
                datasetVersion != null ? datasetVersion : "",
                agentResponsesHash != null ? agentResponsesHash : "",
                consensusJson != null ? consensusJson : "",
                humanDecision != null ? humanDecision : "",
                validatorEmail != null ? validatorEmail : "",
                validateurRole != null ? validateurRole : "",
                validatedAt != null ? validatedAt.toString() : "",
                statutValidation != null ? statutValidation.name() : "",
                previousHash != null ? previousHash : "");
        return HashUtils.sha256(payload);
    }

    public void chainerAvecPrecedent(Decision precedente) {
        this.decisionPrecedente = precedente;
        this.previousHash = precedente != null ? precedente.getCurrentHash() : null;
        this.currentHash = calculerHash();
    }

    public void changerStatut(StatutDecisionEnum statut) {
        this.statutValidation = statut;
    }
}
