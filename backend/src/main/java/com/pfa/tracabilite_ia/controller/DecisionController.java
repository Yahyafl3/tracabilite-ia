package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.config.CreditSchemaConfig;
import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.dto.request.ValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.DecisionExportService;
import com.pfa.tracabilite_ia.service.DecisionService;
import com.pfa.tracabilite_ia.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/decisions")
@PreAuthorize("isAuthenticated()")
public class DecisionController {

    private final DecisionService decisionService;
    private final ValidationService validationService;
    private final AuthService authService;
    private final DecisionExportService decisionExportService;

    public DecisionController(DecisionService decisionService,
                              ValidationService validationService,
                              AuthService authService,
                              DecisionExportService decisionExportService) {
        this.decisionService = decisionService;
        this.validationService = validationService;
        this.authService = authService;
        this.decisionExportService = decisionExportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Decision creer(@Valid @RequestBody DecisionRequest request) {
        return decisionService.creer(request);
    }

    @GetMapping("/credit-schema")
    public Map<String, Object> creditSchema() {
        return CreditSchemaConfig.toSchemaResponse();
    }

    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public DecisionResponse analyser(@Valid @RequestBody CreditFeaturesRequest request) {
        return decisionService.analyserCredit(request);
    }

    @GetMapping
    public DecisionPageResponse lister(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatutDecisionEnum statut,
            @RequestParam(required = false) DecisionDomain domaine,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String decisionFinale,
            @RequestParam(required = false) String validateur,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        return decisionService.rechercher(
                search, statut, domaine, riskLevel, decisionFinale, validateur, from, to, page, size);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) DecisionDomain domaine,
            @RequestParam(required = false) StatutDecisionEnum statut,
            @RequestParam(required = false) String validateur,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime to = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        var user = authService.getCurrentUser();

        boolean excel = "xlsx".equalsIgnoreCase(format) || "xls".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format);
        byte[] body = excel
                ? decisionExportService.exportExcelXml(domaine, statut, from, to, validateur, user)
                : decisionExportService.exportCsv(domaine, statut, from, to, validateur, user);

        String filename = excel ? "decisions-export.xls" : "decisions-export.csv";
        MediaType mediaType = excel
                ? MediaType.parseMediaType("application/vnd.ms-excel")
                : new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(body);
    }

    @GetMapping("/{id}")
    public DecisionResponse obtenir(@PathVariable UUID id) {
        return decisionService.obtenir(id);
    }

    @GetMapping("/{id}/validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'CREDIT_VALIDATOR', 'MEDICAL_VALIDATOR', 'EDUCATION_VALIDATOR')")
    public DecisionResponse obtenirContexteValidation(@PathVariable UUID id) {
        return validationService.obtenirContexteValidation(id);
    }

    @PostMapping("/{id}/submit-validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'VALIDATOR', 'USER')")
    public DecisionResponse soumettreValidation(@PathVariable UUID id) {
        return validationService.soumettreValidation(id);
    }

    @PutMapping("/{id}")
    public DecisionResponse mettreAJour(@PathVariable UUID id,
                                        @Valid @RequestBody DecisionRequest request) {
        return decisionService.mettreAJour(id, request);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'ADMIN')")
    public DecisionResponse approuver(@PathVariable UUID id,
                                      @RequestBody(required = false) @Valid ValidationRequest request) {
        return validationService.approuver(id, request != null ? request : new ValidationRequest());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'ADMIN')")
    public DecisionResponse rejeter(@PathVariable UUID id,
                                    @RequestBody(required = false) @Valid ValidationRequest request) {
        return validationService.rejeter(id, request != null ? request : new ValidationRequest());
    }

    @PostMapping("/{id}/modify")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'ADMIN')")
    public DecisionResponse modifier(@PathVariable UUID id,
                                     @Valid @RequestBody ValidationRequest request) {
        return validationService.modifier(id, request);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('VALIDATOR', 'ADMIN')")
    public DecisionResponse review(@PathVariable UUID id,
                                   @RequestBody(required = false) @Valid ValidationRequest request) {
        return validationService.review(id, request != null ? request : new ValidationRequest());
    }

    @PostMapping("/{id}/retry-failed-agents")
    @PreAuthorize("hasRole('ADMIN')")
    public DecisionResponse retryFailedAgents(@PathVariable UUID id) {
        return decisionService.retryFailedAgents(id, authService.getCurrentUser());
    }
}
