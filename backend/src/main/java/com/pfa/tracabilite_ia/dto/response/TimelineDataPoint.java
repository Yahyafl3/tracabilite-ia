package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDataPoint {
    
    @NotNull(message = "Date cannot be null")
    private LocalDate date;
    
    @Min(value = 0, message = "Decision count cannot be negative")
    private Long decisionCount;
    
    @Min(value = 0, message = "Validation count cannot be negative")
    private Long validationCount;
}
