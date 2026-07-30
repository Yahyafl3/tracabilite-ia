package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCreditDecisionRequest {

    @NotBlank
    private String secteurActivite;

    @NotBlank
    private String region;

    @NotNull @Min(18) @Max(80)
    private Integer ageDemandeur;

    @NotBlank
    private String statutProfessionnel;

    @NotNull @Positive
    private Double revenuMensuelMad;

    @NotNull @Min(0)
    private Double chargesMensuellesMad;

    @NotNull @Positive
    private Double montantDemandeMad;

    @NotNull @Positive
    private Integer dureeCreditMois;

    @NotNull @Min(0)
    private Integer ancienneteProfessionnelleAnnees;

    @NotNull @Min(0)
    private Integer creditsExistants;

    @NotNull @Min(0)
    private Integer incidentsPaiement24Mois;

    @NotNull @DecimalMin("0") @DecimalMax("1")
    private Double ratioEndettement;

    @NotBlank
    private String typeGarantie;

    @NotBlank
    private String typeCredit;

    @Size(max = 4000)
    private String description;

    private Boolean includeAgents = true;
}
