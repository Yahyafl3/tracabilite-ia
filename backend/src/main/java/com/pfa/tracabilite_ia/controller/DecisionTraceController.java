package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.CreateDecisionSourceRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionHistoryResponse;
import com.pfa.tracabilite_ia.dto.response.DecisionSourceResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.service.AuthService;
import com.pfa.tracabilite_ia.service.DecisionHistoryService;
import com.pfa.tracabilite_ia.service.DecisionSourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decisions/{decisionId}")
@PreAuthorize("isAuthenticated()")
public class DecisionTraceController {

    private final DecisionHistoryService decisionHistoryService;
    private final DecisionSourceService decisionSourceService;
    private final AuthService authService;

    public DecisionTraceController(DecisionHistoryService decisionHistoryService,
                                   DecisionSourceService decisionSourceService,
                                   AuthService authService) {
        this.decisionHistoryService = decisionHistoryService;
        this.decisionSourceService = decisionSourceService;
        this.authService = authService;
    }

    @GetMapping("/history")
    public List<DecisionHistoryResponse> history(@PathVariable UUID decisionId) {
        return decisionHistoryService.listByDecision(decisionId);
    }

    @GetMapping("/sources")
    public List<DecisionSourceResponse> sources(@PathVariable UUID decisionId) {
        return decisionSourceService.listByDecision(decisionId);
    }

    @PostMapping("/sources")
    @ResponseStatus(HttpStatus.CREATED)
    public DecisionSourceResponse addSource(@PathVariable UUID decisionId,
                                            @Valid @RequestBody CreateDecisionSourceRequest request) {
        Utilisateur user = authService.getCurrentUser();
        return decisionSourceService.addSource(decisionId, request, user.getId(), user.getEmail());
    }

    @DeleteMapping("/sources/{sourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSource(@PathVariable UUID decisionId, @PathVariable UUID sourceId) {
        Utilisateur user = authService.getCurrentUser();
        decisionSourceService.removeSource(decisionId, sourceId, user.getId(), user.getEmail());
    }
}
