package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DomainValidationRequest {

    @NotBlank
    private String decisionFinale;

    @Size(max = 4000)
    private String justificationHumaine;

    /** Optionnel : true/false ; calculé automatiquement si null. */
    private Boolean accordAvecIa;
}
