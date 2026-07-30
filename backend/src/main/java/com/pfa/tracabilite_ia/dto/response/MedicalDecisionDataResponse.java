package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDecisionDataResponse {
    private String region;
    private Integer age;
    private String sexe;
    private Double imc;
    private String niveauActivitePhysique;
    private String antecedentsFamiliauxDiabete;
    private String hypertension;
    private Double glycemie;
    private String polyurie;
    private String polydipsie;
    private String pertePoidsSoudaine;
    private String faiblesse;
    private String obesite;
    private String suiviMedical;
}
