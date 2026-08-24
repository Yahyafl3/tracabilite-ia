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
public class KPIValues {
    
    @Min(value = 0, message = "Total decisions cannot be negative")
    private Long totalDecisions;
    
    @Min(value = 0, message = "Pending validations cannot be negative")
    private Long pendingValidations;
    
    @Min(value = 0, message = "Today's decisions cannot be negative")
    private Long todaysDecisions;
    
    @Min(value = 0, message = "Active users cannot be negative")
    private Long activeUsers;
    
    @Min(value = 0, message = "Validated decisions cannot be negative")
    private Long validatedDecisions;
    
    @Min(value = 0, message = "Rejected decisions cannot be negative")
    private Long rejectedDecisions;
    
    @Min(value = 0, message = "Acceptance rate cannot be negative")
    private Double acceptanceRate;
    
    @Min(value = 0, message = "Validation rate cannot be negative")
    private Double validationRate;
    
    @Min(value = 0, message = "Processed decisions cannot be negative")
    private Long processedDecisions;
    
    @Min(value = 0, message = "Compliance rate cannot be negative")
    private Double complianceRate;
}
