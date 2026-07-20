package com.pfa.tracabilite_ia.controller;

import com.pfa.tracabilite_ia.dto.response.GroqStatusResponse;
import com.pfa.tracabilite_ia.groq.GroqAgentRegistryService;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/groq")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGroqController {

    private final GroqAgentRegistryService agentRegistryService;
    private final ReponseAgentIARepository reponseAgentIARepository;

    public AdminGroqController(GroqAgentRegistryService agentRegistryService,
                               ReponseAgentIARepository reponseAgentIARepository) {
        this.agentRegistryService = agentRegistryService;
        this.reponseAgentIARepository = reponseAgentIARepository;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        GroqStatusResponse base = agentRegistryService.status();
        long successCount = reponseAgentIARepository.countByProviderAndStatut(
                GroqAgentRegistryService.PROVIDER,
                com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum.SUCCESS
        );

        Map<String, Object> body = new HashMap<>();
        body.put("configured", base.isConfigured());
        body.put("reachable", base.isReachable());
        body.put("models", base.getModels());
        body.put("lastError", base.getLastError());
        body.put("successfulResponses", successCount);
        // Never expose API key
        return ResponseEntity.ok(body);
    }
}
