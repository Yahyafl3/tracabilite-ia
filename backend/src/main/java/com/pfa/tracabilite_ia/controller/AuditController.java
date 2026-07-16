package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.AuditDecisionResponse;
import com.pfa.tracabilite_ia.dto.response.AuditIntegritySummaryResponse;
import com.pfa.tracabilite_ia.dto.response.AuditRecentResponse;
import com.pfa.tracabilite_ia.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/decisions/{decisionId}")
    public AuditDecisionResponse getDecisionAudit(@PathVariable UUID decisionId) {
        return auditService.getDecisionAudit(decisionId);
    }

    @GetMapping("/recent")
    public AuditRecentResponse getRecent(@RequestParam(defaultValue = "20") int limit) {
        return auditService.getRecentAudits(limit);
    }

    @GetMapping("/integrity/summary")
    public AuditIntegritySummaryResponse getIntegritySummary() {
        return auditService.getIntegritySummary();
    }
}
