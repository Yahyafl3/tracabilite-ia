package com.pfa.tracabilite_ia.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pfa.tracabilite_ia.enumeration.CreditSectorEnum;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class CreditFeaturesRequest {

    @NotNull(message = "Le montant est requis")
    @Min(value = 1, message = "Le montant doit être positif")
    private Double amount;

    @NotNull(message = "Le revenu mensuel est requis")
    @Min(value = 1, message = "Le revenu mensuel doit être positif")
    private Double monthlyIncome;

    @NotNull(message = "L'ancienneté de l'entreprise est requise")
    @Min(value = 0, message = "L'ancienneté ne peut pas être négative")
    private Double companyAgeYears;

    @NotNull(message = "Le nombre d'incidents de paiement est requis")
    @Min(value = 0, message = "Les incidents ne peuvent pas être négatifs")
    private Integer paymentIncidents;

    @NotNull(message = "Le ratio d'endettement est requis")
    @DecimalMin(value = "0.0", message = "Le ratio d'endettement minimum est 0")
    @DecimalMax(value = "1.0", message = "Le ratio d'endettement maximum est 1")
    private Double debtRatio;

    @NotNull(message = "Le secteur est requis")
    private CreditSectorEnum sector;

    private String description;

    private boolean includeOpenRouter = true;

    /** Compatibilite ascendante avec l'ancien flag frontend. */
    public boolean isIncludeOllama() {
        return includeOpenRouter;
    }

    public void setIncludeOllama(boolean includeOllama) {
        this.includeOpenRouter = includeOllama;
    }
}
