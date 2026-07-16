package com.pfa.tracabilite_ia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditIntegritySummaryResponse {
    private long totalDecisions;
    private long validDecisions;
    private long invalidDecisions;
    private boolean chainIntact;
    private LocalDateTime generatedAt;
}
