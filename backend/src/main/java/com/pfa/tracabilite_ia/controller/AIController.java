package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.request.DecisionAnalysisRequest;
import com.pfa.tracabilite_ia.dto.response.DecisionAnalysisResponse;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.service.AIService;
import com.pfa.tracabilite_ia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "IA generative", description = "Analyse de decisions via OpenRouter")
@SecurityRequirement(name = "bearerAuth")
public class AIController {

    private final AIService aiService;
    private final AuthService authService;

    public AIController(AIService aiService, AuthService authService) {
        this.aiService = aiService;
        this.authService = authService;
    }

    @GetMapping("/ping")
    @Operation(summary = "Test endpoint public")
    public Map<String, String> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "AI Controller accessible");
        return response;
    }

    @PostMapping("/test-post")
    @Operation(summary = "Diagnostic POST public temporaire")
    public Map<String, String> testPost(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("received", request.toString());
        return response;
    }

    @GetMapping("/security-me")
    @Operation(summary = "Diagnostic authentification et roles")
    public Map<String, Object> securityMe() {
        Utilisateur utilisateur = authService.getCurrentUser();
        List<String> authorities = authService.getCurrentAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("username", utilisateur.getEmail());
        response.put("authorities", authorities);
        return response;
    }

    @PostMapping("/analyze-decision")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Analyser une decision avec OpenRouter")
    public DecisionAnalysisResponse analyzeDecision(@Valid @RequestBody DecisionAnalysisRequest request) {
        return aiService.analyzeDecision(request);
    }
}
