package com.pfa.tracabilite_ia.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "explanation_factor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID factorId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false)
    private Decision decision;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private Double shapValue;

    @Column(nullable = false)
    private String impact;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Double contributionPercent;

    @Column(nullable = false)
    private String source;
}
