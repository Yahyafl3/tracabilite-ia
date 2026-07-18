package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.ai.client.OpenRouterClient;
import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.OpenRouterKeyStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class OpenRouterKeyStatusService {

    private final OpenRouterClient openRouterClient;
    private final OpenRouterProperties properties;

    public OpenRouterKeyStatusService(OpenRouterClient openRouterClient,
                                      OpenRouterProperties properties) {
        this.openRouterClient = openRouterClient;
        this.properties = properties;
    }

    public OpenRouterKeyStatusResponse fetchStatus() {
        if (!properties.isConfigured()) {
            return OpenRouterKeyStatusResponse.builder()
                    .freeTier(false)
                    .dailyUsage(0)
                    .remainingLimit(0d)
                    .available(false)
                    .message("OPENROUTER_API_KEY non configuree")
                    .build();
        }
        return openRouterClient.fetchKeyStatus();
    }

    public boolean hasQuotaForAgents(int agentCount) {
        OpenRouterKeyStatusResponse status = fetchStatus();
        if (!status.isAvailable()) {
            return false;
        }
        if (status.getRemainingLimit() == null) {
            return true;
        }
        return status.getRemainingLimit() >= agentCount;
    }
}
