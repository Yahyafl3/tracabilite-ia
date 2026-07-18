package com.pfa.tracabilite_ia.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reponse_agent_ia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReponseAgentIA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reponseAgentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(nullable = false, length = 32)
    private String agentKey;

    @Column(nullable = false)
    private String modelId;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private String provider;

    private String requestedModelId;

    private String actualModelId;

    private Boolean fallbackUsed;

    @Column(length = 64)
    private String fallbackReason;

    private String responseHash;

    private Integer retryCount;

    @Column(columnDefinition = "TEXT")
    private String reponseBrute;

    @Column(columnDefinition = "TEXT")
    private String reponseNormalisee;

    private String decisionProposee;

    private Double confianceDeclaree;

    private String niveauRisque;

    @Column(columnDefinition = "TEXT")
    private String resume;

    @Column(columnDefinition = "TEXT")
    private String explication;

    @Column(columnDefinition = "TEXT")
    private String recommandationsJson;

    private Long dureeMs;

    private Integer nombreTokens;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReponseAgentEnum statut;

    @Column(length = 64)
    private String codeErreur;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
