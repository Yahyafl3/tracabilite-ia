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

    @Column(nullable = false, length = 32)
    private String secteurActivite;

    @Column(nullable = false, length = 64)
    private String region;

    private Integer ageDemandeur;

    @Column(length = 32)
    private String statutProfessionnel;

    private Double revenuMensuelMad;
    private Double chargesMensuellesMad;
    private Double montantDemandeMad;
    private Integer dureeCreditMois;
    private Integer ancienneteProfessionnelleAnnees;
    private Integer creditsExistants;
    private Integer incidentsPaiement24Mois;
    private Double ratioEndettement;

    @Column(length = 32)
    private String typeGarantie;

    @Column(length = 32)
    private String typeCredit;
}
