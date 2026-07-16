package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.config.CreditSchemaConfig;
import com.pfa.tracabilite_ia.dto.request.CreditFeaturesRequest;
import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.dto.request.ValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.service.DecisionService;
import com.pfa.tracabilite_ia.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/decisions")
@PreAuthorize("isAuthenticated()")
public class DecisionController {

    private final DecisionService decisionService;
    private final ValidationService validationService;

    public DecisionController(DecisionService decisionService,
                              ValidationService validationService) {
        this.decisionService = decisionService;
        this.validationService = validationService;
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
    public DecisionPageResponse lister(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) StatutDecisionEnum statut,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return decisionService.rechercher(search, statut, page, size);
    }

    @GetMapping("/{id}")
    public DecisionResponse obtenir(@PathVariable UUID id) {
        return decisionService.obtenir(id);
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
}
