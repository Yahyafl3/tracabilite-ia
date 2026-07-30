package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateEducationDecisionRequest {

    @NotBlank
    private String region;

    @NotBlank
    private String typeEtablissement;

    @NotBlank
    private String filiere;

    @NotBlank
    private String niveauEtude;

    @NotNull @DecimalMin("0") @DecimalMax("20")
    private Double moyenneSemestre1;

    @NotNull @DecimalMin("0") @DecimalMax("20")
    private Double moyenneSemestre2;

    @NotNull @DecimalMin("0") @DecimalMax("100")
    private Double tauxAbsence;

    @NotNull @Min(0)
    private Integer modulesNonValides;

    @NotBlank
    private String participation;

    @NotBlank
    private String bourse;

    @NotNull @Min(0)
    private Double distanceLogementKm;

    @NotBlank
    private String accesInternet;

    @NotBlank
    private String activiteProfessionnelle;

    @NotBlank
    private String historiqueRedoublement;

    @NotBlank
    private String situationAcademique;

    @Size(max = 4000)
    private String description;

    private Boolean includeAgents = true;
}
