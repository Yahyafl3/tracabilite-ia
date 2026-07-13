package com.pfa.tracabilite_ia.dto.request;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class DecisionRequest {

    @NotBlank(message = "Le prompt est requis")
    private String prompt;

    private String contexte;

    @NotBlank(message = "Le nom du modele est requis")
    private String modelName;

    private String modelVersion;

    @NotBlank(message = "La reponse est requise")
    private String reponse;

    private UUID systemeIaId;

    private StatutDecisionEnum statutValidation;
}
