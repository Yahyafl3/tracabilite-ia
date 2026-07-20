package com.pfa.tracabilite_ia.groq;

import com.pfa.tracabilite_ia.ai.client.GroqClient;
import com.pfa.tracabilite_ia.config.GroqProperties;
import com.pfa.tracabilite_ia.dto.response.GroqModelStatusResponse;
import com.pfa.tracabilite_ia.dto.response.GroqStatusResponse;
import com.pfa.tracabilite_ia.exception.GroqErrorCode;
import com.pfa.tracabilite_ia.exception.GroqException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class GroqAgentRegistryService {

    public static final String AGENT_1 = "AGENT_1";
    public static final String AGENT_2 = "AGENT_2";
    public static final String AGENT_3 = "AGENT_3";

    public static final String PROVIDER = "GROQ";

    public static final String MODEL_1_ID = "llama-3.3-70b-versatile";
    public static final String MODEL_2_ID = "openai/gpt-oss-120b";
    public static final String MODEL_3_ID = "openai/gpt-oss-20b";

    public static final String DISPLAY_1 = "Llama 3.3 70B Versatile";
    public static final String DISPLAY_2 = "GPT-OSS 120B";
    public static final String DISPLAY_3 = "GPT-OSS 20B";

    private final GroqProperties properties;
    private final GroqClient groqClient;

    public GroqAgentRegistryService(GroqProperties properties, GroqClient groqClient) {
        this.properties = properties;
        this.groqClient = groqClient;
    }

    public List<GroqAgentDefinition> configuredAgents() {
        return List.of(
                new GroqAgentDefinition(AGENT_1, DISPLAY_1, properties.getModel1(), PROVIDER, 1, true),
                new GroqAgentDefinition(AGENT_2, DISPLAY_2, properties.getModel2(), PROVIDER, 2, true),
                new GroqAgentDefinition(AGENT_3, DISPLAY_3, properties.getModel3(), PROVIDER, 3, true)
        ).stream()
                .filter(GroqAgentDefinition::active)
                .sorted(Comparator.comparingInt(GroqAgentDefinition::orderIndex))
                .toList();
    }

    public GroqAgentDefinition findAgent(String agentKey) {
        return configuredAgents().stream()
                .filter(agent -> agent.agentKey().equals(agentKey))
                .findFirst()
                .orElseThrow(() -> new GroqException(
                        GroqErrorCode.GROQ_MODEL_NOT_FOUND,
                        "Agent inconnu: " + agentKey
                ));
    }

    public GroqStatusResponse status() {
        boolean configured = properties.isConfigured();
        boolean reachable = false;
        Set<String> available = Set.of();
        String lastError = null;

        if (configured) {
            try {
                available = groqClient.listAvailableModelIds();
                reachable = true;
            } catch (Exception ex) {
                lastError = ex.getMessage();
                reachable = false;
            }
        }

        List<GroqModelStatusResponse> models = new ArrayList<>();
        for (GroqAgentDefinition agent : configuredAgents()) {
            boolean modelConfigured = agent.modelId() != null && !agent.modelId().isBlank();
            boolean availableFlag = reachable && modelConfigured
                    && (available.isEmpty() || available.contains(agent.modelId()));
            models.add(GroqModelStatusResponse.builder()
                    .agent(agent.agentKey())
                    .displayName(agent.displayName())
                    .modelId(agent.modelId())
                    .available(availableFlag)
                    .build());
        }

        return GroqStatusResponse.builder()
                .configured(configured)
                .reachable(reachable)
                .lastError(lastError)
                .models(models)
                .build();
    }
}
