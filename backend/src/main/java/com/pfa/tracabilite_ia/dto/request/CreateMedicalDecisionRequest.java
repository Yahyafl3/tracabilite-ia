package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMedicalDecisionRequest {

    @NotBlank
    private String region;

    @NotNull @Min(1) @Max(120)
    private Integer age;

    @NotBlank
    private String sexe;

    @NotNull @DecimalMin("10") @DecimalMax("60")
    private Double imc;

    @NotBlank
    private String niveauActivitePhysique;

    @NotBlank
    private String antecedentsFamiliauxDiabete;

    @NotBlank
    private String hypertension;

    @NotNull @Positive
    private Double glycemie;

    @NotBlank
    private String polyurie;

    @NotBlank
    private String polydipsie;

    @NotBlank
    private String pertePoidsSoudaine;

    @NotBlank
    private String faiblesse;

    @NotBlank
    private String obesite;

    @NotBlank
    private String suiviMedical;

    @Size(max = 4000)
    private String description;

    private Boolean includeAgents = true;
}
