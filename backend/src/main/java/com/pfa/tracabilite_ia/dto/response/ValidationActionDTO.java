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
public class ValidationActionDTO {
    
    @NotNull(message = "Decision ID cannot be null")
    private UUID decisionId;
    
    @NotBlank(message = "Reference cannot be blank")
    private String decisionReference;
    
    @NotNull(message = "Domain cannot be null")
    private DecisionDomain domaine;
    
    @NotNull(message = "Status cannot be null")
    private StatutDecisionEnum statutValidation;
    
    @NotNull(message = "Validation date cannot be null")
    private LocalDateTime validatedAt;
    
    @NotBlank(message = "Validator email cannot be blank")
    private String validatedBy;
    
    private String validatorName;
    
    private String commentaire;
}
