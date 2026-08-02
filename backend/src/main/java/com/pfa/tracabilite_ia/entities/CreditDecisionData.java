package com.pfa.tracabilite_ia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "credit_decision_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDecisionData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false, unique = true)
    private Decision decision;

    private Integer age;

    private Integer dureeMois;

    @Column(length = 32)
    private String typeContrat;

    @Column(length = 32)
    private String statutLogement;

    private Integer incidentPaiementBam;
    private Double montantDemandeMad;
    private Double nouvelleEcheanceMad;
    private Double revenuMensuelMad;
    private Double tauxEndettement;
}
