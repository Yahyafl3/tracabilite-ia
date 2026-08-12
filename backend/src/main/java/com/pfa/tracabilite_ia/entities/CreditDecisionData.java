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

    @Column(name = "duree_mois")
    private Integer dureeMois;

    @Column(name = "type_contrat", length = 32)
    private String typeContrat;

    @Column(name = "statut_logement", length = 32)
    private String statutLogement;

    @Column(name = "incident_paiement_bam")
    private Integer incidentPaiementBam;
    
    @Column(name = "montant_demande_mad")
    private Double montantDemandeMad;
    
    @Column(name = "nouvelle_echeance_mad")
    private Double nouvelleEcheanceMad;
    
    @Column(name = "revenu_mensuel_mad")
    private Double revenuMensuelMad;
    
    @Column(name = "taux_endettement")
    private Double tauxEndettement;
}
