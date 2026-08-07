package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.CreateCreditDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateEducationDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.CreateMedicalDecisionRequest;
import com.pfa.tracabilite_ia.dto.request.DomainValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.service.DecisionOrchestratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints multidomain (CREDIT / MEDICAL / EDUCATION).
 * Les endpoints legacy {@code /api/decisions/analyze} restent disponibles.
 */
@RestController
@RequestMapping("/api/decisions")
@PreAuthorize("isAuthenticated()")
public class MultidomainDecisionController {

    private final DecisionOrchestratorService orchestrator;
    private final com.pfa.tracabilite_ia.repository.DecisionRepository decisionRepository;
    private final com.pfa.tracabilite_ia.service.DecisionHashService decisionHashService;

    public MultidomainDecisionController(
            DecisionOrchestratorService orchestrator,
            com.pfa.tracabilite_ia.repository.DecisionRepository decisionRepository,
            com.pfa.tracabilite_ia.service.DecisionHashService decisionHashService
    ) {
        this.orchestrator = orchestrator;
        this.decisionRepository = decisionRepository;
        this.decisionHashService = decisionHashService;
    }

    @PostMapping("/credit")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DecisionResponse createCredit(@Valid @RequestBody CreateCreditDecisionRequest request) {
        return orchestrator.createAndAnalyzeCredit(request);
    }

    @PostMapping("/medical")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DecisionResponse createMedical(@Valid @RequestBody CreateMedicalDecisionRequest request) {
        return orchestrator.createAndAnalyzeMedical(request);
    }

    @PostMapping("/education")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DecisionResponse createEducation(@Valid @RequestBody CreateEducationDecisionRequest request) {
        return orchestrator.createAndAnalyzeEducation(request);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VALIDATOR')")
    public DecisionResponse submit(@PathVariable UUID id) {
        return orchestrator.submit(id);
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE')")
    public DecisionResponse validate(
            @PathVariable UUID id,
            @Valid @RequestBody DomainValidationRequest request
    ) {
        return orchestrator.validate(id, request);
    }

    @PostMapping("/{id}/request-review")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE')")
    public DecisionResponse requestReview(
            @PathVariable UUID id,
            @RequestBody(required = false) DomainValidationRequest request
    ) {
        return orchestrator.requestReview(id, request != null ? request : new DomainValidationRequest());
    }

    @GetMapping("/domain/{domain}")
    public List<DecisionResponse> byDomain(@PathVariable DecisionDomain domain) {
        return orchestrator.byDomain(domain);
    }

    @GetMapping("/pending-validation")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE', 'ADMIN')")
    public List<DecisionResponse> pendingValidation() {
        return orchestrator.pendingValidation();
    }

    /**
     * Vérifie l'intégrité SHA-256 d'une décision.
     * Retours : VALID | INVALID | NOT_AVAILABLE
     */
    @PostMapping("/{id}/verify-integrity")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'VALIDATOR', 'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE')")
    public java.util.Map<String, Object> verifyIntegrity(@PathVariable UUID id) {
        return decisionRepository.findById(id)
                .map(decision -> {
                    if (decision.getCurrentHash() == null || decision.getCurrentHash().isBlank()) {
                        return java.util.Map.<String, Object>of(
                                "decisionId", id.toString(),
                                "status", "NOT_AVAILABLE",
                                "integrityValid", false
                        );
                    }
                    boolean valid = decisionHashService.verifyDecisionIntegrity(decision);
                    return java.util.Map.<String, Object>of(
                            "decisionId", id.toString(),
                            "status", valid ? "VALID" : "INVALID",
                            "integrityValid", valid
                    );
                })
                .orElse(java.util.Map.of(
                        "decisionId", id.toString(),
                        "status", "NOT_AVAILABLE",
                        "integrityValid", false
                ));
    }
}
