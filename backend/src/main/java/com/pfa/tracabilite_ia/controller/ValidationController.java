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
@PreAuthorize("hasAnyRole('VALIDATOR', 'ADMIN')")
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/pending")
    public DecisionPageResponse listerEnAttente(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return validationService.listerEnAttente(page, size);
    }

    @GetMapping("/decision/{decisionId}/history")
    public List<ValidationActionResponse> historique(@PathVariable UUID decisionId) {
        return validationService.historique(decisionId);
    }
}
