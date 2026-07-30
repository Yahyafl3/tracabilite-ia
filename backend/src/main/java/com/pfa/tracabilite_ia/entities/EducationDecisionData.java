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

    @Column(nullable = false, length = 64)
    private String region;

    @Column(length = 48)
    private String typeEtablissement;

    @Column(length = 32)
    private String filiere;

    @Column(length = 8)
    private String niveauEtude;

    private Double moyenneSemestre1;
    private Double moyenneSemestre2;
    private Double tauxAbsence;
    private Integer modulesNonValides;

    @Column(length = 16)
    private String participation;

    @Column(length = 8)
    private String bourse;

    private Double distanceLogementKm;

    @Column(length = 8)
    private String accesInternet;

    @Column(length = 8)
    private String activiteProfessionnelle;

    @Column(length = 8)
    private String historiqueRedoublement;

    @Column(length = 32)
    private String situationAcademique;
}
