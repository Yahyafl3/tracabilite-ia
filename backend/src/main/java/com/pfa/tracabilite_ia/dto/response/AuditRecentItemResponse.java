package com.pfa.tracabilite_ia.dto.response;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecentItemResponse {
    private UUID decisionId;
    private String prompt;
    private StatutDecisionEnum statutValidation;
    private boolean integrityValid;
    private LocalDateTime timestamp;
}
