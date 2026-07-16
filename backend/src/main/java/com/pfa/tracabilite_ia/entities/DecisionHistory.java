package com.pfa.tracabilite_ia.entities;

import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "decision_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionHistoryAction action;

    @Enumerated(EnumType.STRING)
    private StatutDecisionEnum previousStatus;

    @Enumerated(EnumType.STRING)
    private StatutDecisionEnum newStatus;

    private UUID performedById;

    @Column(length = 255)
    private String performedByEmail;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Column(columnDefinition = "TEXT")
    private String eventDataJson;

    @Column(length = 64)
    private String correlationId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
