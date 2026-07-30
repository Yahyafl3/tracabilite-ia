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
    private String secteurActivite;
    private String region;
    private Integer ageDemandeur;
    private String statutProfessionnel;
    private Double revenuMensuelMad;
    private Double chargesMensuellesMad;
    private Double montantDemandeMad;
    private Integer dureeCreditMois;
    private Integer ancienneteProfessionnelleAnnees;
    private Integer creditsExistants;
    private Integer incidentsPaiement24Mois;
    private Double ratioEndettement;
    private String typeGarantie;
    private String typeCredit;
}
