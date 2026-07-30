package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDecisionDataResponse {
    private String region;
    private String typeEtablissement;
    private String filiere;
    private String niveauEtude;
    private Double moyenneSemestre1;
    private Double moyenneSemestre2;
    private Double tauxAbsence;
    private Integer modulesNonValides;
    private String participation;
    private String bourse;
    private Double distanceLogementKm;
    private String accesInternet;
    private String activiteProfessionnelle;
    private String historiqueRedoublement;
    private String situationAcademique;
}
