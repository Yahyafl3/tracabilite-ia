package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateMedicalDecisionRequest {

    @NotNull @Min(1) @Max(120)
    private Integer age;

    @NotNull @Min(0)
    private Integer grossesses;

    @NotNull @DecimalMin("0")
    private Double glycemieMgDl;

    @NotNull @DecimalMin("0")
    private Double pressionArterielleMmhg;

    @NotNull @DecimalMin("0")
    private Double epaisseurPliCutaneMm;

    @NotNull @DecimalMin("0")
    private Double insulineMicroUMl;

    @NotNull @DecimalMin("10") @DecimalMax("80")
    private Double imcKgM2;

    @Size(max = 4000)
    private String description;

    private Boolean includeAgents = true;
}
