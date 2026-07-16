package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.response.ComparaisonAgentResponse;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentDefinition;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRegistryService;
import com.pfa.tracabilite_ia.repository.ReponseAgentIARepository;
import com.pfa.tracabilite_ia.service.ComparaisonService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ComparaisonServiceImpl implements ComparaisonService {

    private final ReponseAgentIARepository reponseAgentIARepository;
    private final OpenRouterAgentRegistryService openRouterAgentRegistryService;

    public ComparaisonServiceImpl(ReponseAgentIARepository reponseAgentIARepository,
                                  OpenRouterAgentRegistryService openRouterAgentRegistryService) {
        this.reponseAgentIARepository = reponseAgentIARepository;
        this.openRouterAgentRegistryService = openRouterAgentRegistryService;
    }

    @Override
    public List<ComparaisonAgentResponse> classerAgentsOpenRouter() {
        List<ComparaisonAgentResponse> resultats = new ArrayList<>();

        for (OpenRouterAgentDefinition agent : openRouterAgentRegistryService.configuredAgents()) {
            long total = reponseAgentIARepository.countByAgentKey(agent.agentKey());
            long reussies = reponseAgentIARepository.countByAgentKeyAndStatut(
                    agent.agentKey(), StatutReponseAgentEnum.SUCCESS);
            long approuver = reponseAgentIARepository.countSuccessfulByAgentKeyAndDecision(
                    agent.agentKey(), "APPROUVER");
            long rejeter = reponseAgentIARepository.countSuccessfulByAgentKeyAndDecision(
                    agent.agentKey(), "REJETER");
            long review = reponseAgentIARepository.countSuccessfulByAgentKeyAndDecision(
                    agent.agentKey(), "REVIEW");

            double score = total == 0 ? 0.0d : BigDecimal.valueOf((reussies * 100.0d) / total)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            resultats.add(new ComparaisonAgentResponse(
                    null,
                    null,
                    agent.displayName(),
                    agent.provider(),
                    agent.modelId(),
                    "openrouter",
                    total,
                    approuver,
                    review,
                    rejeter,
                    total - reussies,
                    score));
        }

        resultats.sort(Comparator
                .comparingDouble(ComparaisonAgentResponse::getScorePourcentage).reversed()
                .thenComparing(ComparaisonAgentResponse::getNom, String.CASE_INSENSITIVE_ORDER));

        for (int index = 0; index < resultats.size(); index++) {
            resultats.get(index).setRang(index + 1);
        }
        return resultats;
    }
}
