package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.ValidationRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionResponse;
import com.pfa.tracabilite_ia.dto.response.ValidationActionResponse;
import com.pfa.tracabilite_ia.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/validation")
@PreAuthorize("hasAnyRole('VALIDATOR', 'ADMIN', 'RESPONSABLE_CREDIT', 'PROFESSIONNEL_SANTE', 'RESPONSABLE_PEDAGOGIQUE')")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /** Liste paginée des décisions en attente de validation. */
    @GetMapping("/pending")
    public DecisionPageResponse listerEnAttente(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return validationService.listerEnAttente(page, size);
    }

    /** Détail complet d'une décision pour la page de validation. */
    @GetMapping("/{id}")
    public DecisionResponse obtenirPourValidation(@PathVariable UUID id) {
        return validationService.obtenirContexteValidation(id);
    }

    /** Historique des actions de validation pour une décision. */
    @GetMapping("/decision/{decisionId}/history")
    public List<ValidationActionResponse> historique(@PathVariable UUID decisionId) {
        return validationService.historique(decisionId);
    }
}
