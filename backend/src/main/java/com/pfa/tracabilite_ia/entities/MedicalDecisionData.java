package com.pfa.tracabilite_ia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "medical_decision_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDecisionData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false, unique = true)
    private Decision decision;

    private Integer age;
    private Integer grossesses;
    
    @Column(name = "glycemie_mg_dl")
    private Double glycemieMgDl;
    
    @Column(name = "pression_arterielle_mmhg")
    private Double pressionArterielleMmhg;
    
    @Column(name = "epaisseur_pli_cutane_mm")
    private Double epaisseurPliCutaneMm;
    
    @Column(name = "insuline_micro_u_ml")
    private Double insulineMicroUMl;
    
    @Column(name = "imc_kg_m2")
    private Double imcKgM2;
}
