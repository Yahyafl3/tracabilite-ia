package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.config.OpenRouterProperties;
import com.pfa.tracabilite_ia.dto.response.OpenRouterModelStatusResponse;
import com.pfa.tracabilite_ia.exception.OpenRouterErrorCode;
import com.pfa.tracabilite_ia.exception.OpenRouterException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class OpenRouterAgentRegistryService {

    public static final String AGENT_1 = "AGENT_1";
    public static final String AGENT_2 = "AGENT_2";
    public static final String AGENT_3 = "AGENT_3";

    public static final String MODEL_1_ID = "meta-llama/llama-3.3-70b-instruct:free";
    public static final String MODEL_2_ID = "google/gemma-4-26b-a4b-it:free";
    public static final String MODEL_3_ID = "openai/gpt-oss-20b:free";
    public static final String LEGACY_MODEL_2_ID = "google/gemma-4-31b-it:free";
    public static final String LEGACY_MODEL_3_ID = "openai/gpt-oss-120b:free";

    private final OpenRouterProperties properties;
    private final OpenRouterModelsCacheService modelsCacheService;

    public OpenRouterAgentRegistryService(OpenRouterProperties properties,
                                          OpenRouterModelsCacheService modelsCacheService) {
        this.properties = properties;
        this.modelsCacheService = modelsCacheService;
    }

    public List<OpenRouterAgentDefinition> configuredAgents() {
        return List.of(
                agentDefinition(AGENT_1, properties.getModel1(), "META_OPENROUTER", 1),
                agentDefinition(AGENT_2, properties.getModel2(), "GOOGLE_OPENROUTER", 2),
                agentDefinition(AGENT_3, properties.getModel3(), "OPENAI_OPENROUTER", 3)
        ).stream()
                .filter(OpenRouterAgentDefinition::active)
                .sorted(Comparator.comparingInt(OpenRouterAgentDefinition::orderIndex))
                .toList();
    }

    private OpenRouterAgentDefinition agentDefinition(String agentKey, String modelId, String provider, int order) {
        return new OpenRouterAgentDefinition(
                agentKey,
                OpenRouterAgentDisplayNames.resolve(modelId),
                modelId,
                provider,
                order,
                true
        );
    }

    public List<OpenRouterModelStatusResponse> modelStatuses() {
        Set<String> availableModelIds = fetchAvailableModelIdsSafely();
        List<OpenRouterModelStatusResponse> statuses = new ArrayList<>();

        for (OpenRouterAgentDefinition agent : configuredAgents()) {
            boolean configured = agent.modelId() != null && !agent.modelId().isBlank();
            boolean available = configured && availableModelIds.contains(agent.modelId());
            String status;
            if (!configured) {
                status = "NOT_CONFIGURED";
            } else if (!properties.isConfigured()) {
                status = "AUTHENTICATION_FAILED";
            } else if (availableModelIds.isEmpty()) {
                status = "STATUS_UNKNOWN";
            } else if (available) {
                status = "AVAILABLE";
            } else {
                status = "MODEL_NOT_LISTED";
            }

            statuses.add(OpenRouterModelStatusResponse.builder()
                    .agentKey(agent.agentKey())
                    .displayName(agent.displayName())
                    .modelId(agent.modelId())
                    .provider(agent.provider())
                    .configured(configured)
                    .available(available)
                    .status(status)
                    .build());
        }
        return statuses;
    }

    public boolean isAgentAvailable(String agentKey, Set<String> availableModelIds) {
        return configuredAgents().stream()
                .filter(agent -> agent.agentKey().equals(agentKey))
                .findFirst()
                .map(agent -> availableModelIds.contains(agent.modelId()))
                .orElse(false);
    }

    public Set<String> fetchAvailableModelIds() {
        return modelsCacheService.availableModelIds();
    }

    private Set<String> fetchAvailableModelIdsSafely() {
        if (!properties.isConfigured()) {
            return Set.of();
        }
        try {
            return fetchAvailableModelIds();
        } catch (Exception ex) {
            return Set.of();
        }
    }

    public OpenRouterAgentDefinition findAgent(String agentKey) {
        return configuredAgents().stream()
                .filter(agent -> agent.agentKey().equals(agentKey))
                .findFirst()
                .orElseThrow(() -> new OpenRouterException(
                        OpenRouterErrorCode.OPENROUTER_MODEL_NOT_FOUND,
                        "Agent inconnu: " + agentKey
                ));
    }
}
