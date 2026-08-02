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
    private String sexe;
    private String boursier;
    private String fraisAJour;
    private String debiteur;
    private String deplace;
    private String international;
}
