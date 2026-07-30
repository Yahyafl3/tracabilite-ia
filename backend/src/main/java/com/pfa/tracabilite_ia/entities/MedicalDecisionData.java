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

    @Column(nullable = false, length = 64)
    private String region;

    private Integer age;

    @Column(length = 16)
    private String sexe;

    private Double imc;

    @Column(length = 32)
    private String niveauActivitePhysique;

    @Column(length = 8)
    private String antecedentsFamiliauxDiabete;

    @Column(length = 8)
    private String hypertension;

    private Double glycemie;

    @Column(length = 8)
    private String polyurie;

    @Column(length = 8)
    private String polydipsie;

    @Column(length = 8)
    private String pertePoidsSoudaine;

    @Column(length = 8)
    private String faiblesse;

    @Column(length = 8)
    private String obesite;

    @Column(length = 8)
    private String suiviMedical;
}
