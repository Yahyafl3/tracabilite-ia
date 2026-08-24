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

    @Column(name = "age_inscription")
    private Integer ageInscription;
    
    @Column(name = "note_admission")
    private Double noteAdmission;
    
    @Column(name = "note_qualification_precedente")
    private Double noteQualificationPrecedente;
    
    @Column(name = "unites_validees_s1")
    private Integer unitesValideesS1;
    
    @Column(name = "moyenne_s1")
    private Double moyenneS1;
    
    @Column(name = "unites_validees_s2")
    private Integer unitesValideesS2;
    
    @Column(name = "moyenne_s2")
    private Double moyenneS2;
    
    @Column(name = "taux_chomage")
    private Double tauxChomage;
    
    @Column(name = "taux_inflation")
    private Double tauxInflation;
    
    private Double pib;

    @Column(length = 16)
    private String sexe;

    @Column(length = 8)
    private String boursier;

    @Column(name = "frais_a_jour", length = 8)
    private String fraisAJour;

    @Column(length = 8)
    private String debiteur;

    @Column(length = 8)
    private String deplace;

    @Column(length = 8)
    private String international;
}
