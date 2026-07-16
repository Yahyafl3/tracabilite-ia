package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.AuditDecisionResponse;
import com.pfa.tracabilite_ia.dto.response.AuditIntegritySummaryResponse;
import com.pfa.tracabilite_ia.dto.response.AuditRecentResponse;

import java.util.UUID;

public interface AuditService {

    AuditDecisionResponse getDecisionAudit(UUID decisionId);

    AuditRecentResponse getRecentAudits(int limit);

    AuditIntegritySummaryResponse getIntegritySummary();
}
