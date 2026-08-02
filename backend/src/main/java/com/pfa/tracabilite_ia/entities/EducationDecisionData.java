package com.pfa.tracabilite_ia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "education_decision_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationDecisionData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decision_id", nullable = false, unique = true)
    private Decision decision;

    private Integer ageInscription;
    private Double noteAdmission;
    private Double noteQualificationPrecedente;
    private Integer unitesValideesS1;
    private Double moyenneS1;
    private Integer unitesValideesS2;
    private Double moyenneS2;
    private Double tauxChomage;
    private Double tauxInflation;
    private Double pib;

    @Column(length = 16)
    private String sexe;

    @Column(length = 8)
    private String boursier;

    @Column(length = 8)
    private String fraisAJour;

    @Column(length = 8)
    private String debiteur;

    @Column(length = 8)
    private String deplace;

    @Column(length = 8)
    private String international;
}
