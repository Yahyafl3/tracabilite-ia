package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCreditDecisionRequest {

    @NotNull @Min(18) @Max(100)
    private Integer age;

    @NotNull @Positive
    private Integer dureeMois;

    @NotBlank
    private String typeContrat;

    @NotBlank
    private String statutLogement;

    @NotNull @Min(0)
    private Integer incidentPaiementBam;

    @NotNull @Positive
    private Double montantDemandeMad;

    @NotNull @DecimalMin("0")
    private Double nouvelleEcheanceMad;

    @NotNull @Positive
    private Double revenuMensuelMad;

    @NotNull @DecimalMin("0") @DecimalMax("1")
    private Double tauxEndettement;

    @Size(max = 4000)
    private String description;

    private Boolean includeAgents = true;
}
