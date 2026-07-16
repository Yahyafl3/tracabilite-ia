package com.pfa.tracabilite_ia.entities;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validation_action")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID validationActionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "validateur_id", nullable = false)
    private Utilisateur validateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeActionEnum typeAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDecisionEnum statutAvant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDecisionEnum statutApres;

    @Column(length = 32)
    private String decisionHumaine;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
