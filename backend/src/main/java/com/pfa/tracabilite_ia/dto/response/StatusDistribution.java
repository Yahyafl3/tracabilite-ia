package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistribution {
    
    @Min(value = 0, message = "EN_ATTENTE_VALIDATION count cannot be negative")
    private Long enAttenteValidation;
    
    @Min(value = 0, message = "VALIDEE count cannot be negative")
    private Long validee;
    
    @Min(value = 0, message = "REJETEE count cannot be negative")
    private Long rejetee;
}
