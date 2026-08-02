package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDecisionDataResponse {
    private Integer age;
    private Integer grossesses;
    private Double glycemieMgDl;
    private Double pressionArterielleMmhg;
    private Double epaisseurPliCutaneMm;
    private Double insulineMicroUMl;
    private Double imcKgM2;
}
