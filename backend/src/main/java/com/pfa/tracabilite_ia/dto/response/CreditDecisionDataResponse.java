package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditDecisionDataResponse {
    private Integer age;
    private Integer dureeMois;
    private String typeContrat;
    private String statutLogement;
    private Integer incidentPaiementBam;
    private Double montantDemandeMad;
    private Double nouvelleEcheanceMad;
    private Double revenuMensuelMad;
    private Double tauxEndettement;
}
