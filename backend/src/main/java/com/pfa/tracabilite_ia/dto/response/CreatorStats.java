package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorStats {
    
    @NotBlank(message = "Creator email cannot be blank")
    @Email(message = "Creator email must be valid")
    private String creatorEmail;
    
    @NotBlank(message = "Creator name cannot be blank")
    private String creatorName;
    
    @Min(value = 0, message = "Decision count cannot be negative")
    private Long decisionCount;
}
