package com.pfa.tracabilite_ia.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDecisionEnum statutValidation = StatutDecisionEnum.EN_ATTENTE;

    @Column(length = 64)
    private String previousHash;

    @Column(length = 64)
    private String currentHash;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_precedente_id")
    private Decision decisionPrecedente;

    public String calculerHash() {
        String payload = String.join("|",
                decisionId != null ? decisionId.toString() : "",
                timestamp != null ? timestamp.toString() : "",
                prompt != null ? prompt : "",
                contexte != null ? contexte : "",
                modelName != null ? modelName : "",
                modelVersion != null ? modelVersion : "",
                reponse != null ? reponse : "",
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
