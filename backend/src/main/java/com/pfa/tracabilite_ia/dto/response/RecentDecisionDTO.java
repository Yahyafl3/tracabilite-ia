package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentDecisionDTO {
    
    @NotNull(message = "Decision ID cannot be null")
    private UUID decisionId;
    
    @NotBlank(message = "Reference cannot be blank")
    private String reference;
    
    @NotNull(message = "Domain cannot be null")
    private DecisionDomain domaine;
    
    @NotNull(message = "Status cannot be null")
    private StatutDecisionEnum statutValidation;
    
    @NotNull(message = "Creation date cannot be null")
    private LocalDateTime createdAt;
    
    @NotBlank(message = "Creator email cannot be blank")
    private String createdBy;
    
    private String creatorName;
}
