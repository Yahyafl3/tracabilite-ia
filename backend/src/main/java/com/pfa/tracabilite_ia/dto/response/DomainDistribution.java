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
public class DomainDistribution {
    
    @Min(value = 0, message = "Credit count cannot be negative")
    private Long creditCount;
    
    @Min(value = 0, message = "Medical count cannot be negative")
    private Long medicalCount;
    
    @Min(value = 0, message = "Education count cannot be negative")
    private Long educationCount;
}
