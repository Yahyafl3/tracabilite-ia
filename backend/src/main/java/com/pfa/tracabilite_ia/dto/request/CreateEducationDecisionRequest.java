package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateEducationDecisionRequest {

    @NotNull @Min(15) @Max(80)
    private Integer ageInscription;

    @NotNull
    private Double noteAdmission;

    @NotNull
    private Double noteQualificationPrecedente;

    @NotNull @Min(0)
    private Integer unitesValideesS1;

    @NotNull @DecimalMin("0") @DecimalMax("20")
    private Double moyenneS1;

    @NotNull @Min(0)
    private Integer unitesValideesS2;

    @NotNull @DecimalMin("0") @DecimalMax("20")
    private Double moyenneS2;

    @NotNull
    private Double tauxChomage;

    @NotNull
    private Double tauxInflation;

    @NotNull
    private Double pib;

    @NotBlank
    private String sexe;

    @NotBlank
    private String boursier;

    @NotBlank
    private String fraisAJour;

    @NotBlank
    private String debiteur;

    @NotBlank
    private String deplace;

    @NotBlank
    private String international;

    @Size(max = 4000)
    private String description;

    private Boolean includeAgents = true;
}
