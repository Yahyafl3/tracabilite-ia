package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.response.ComparaisonAgentResponse;
import com.pfa.tracabilite_ia.dto.response.DashboardResponse;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentDefinition;
import com.pfa.tracabilite_ia.openrouter.OpenRouterAgentRegistryService;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.service.ComparaisonService;
import com.pfa.tracabilite_ia.service.DashboardService;
import com.pfa.tracabilite_ia.service.HashChainService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_LIMIT = 10;

    private final DecisionRepository decisionRepository;
    private final ComparaisonService comparaisonService;
    private final HashChainService hashChainService;
    private final OpenRouterAgentRegistryService openRouterAgentRegistryService;

    public DashboardServiceImpl(DecisionRepository decisionRepository,
                                ComparaisonService comparaisonService,
                                HashChainService hashChainService,
                                OpenRouterAgentRegistryService openRouterAgentRegistryService) {
        this.decisionRepository = decisionRepository;
        this.comparaisonService = comparaisonService;
        this.hashChainService = hashChainService;
        this.openRouterAgentRegistryService = openRouterAgentRegistryService;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenirStatistiques() {
        long total = decisionRepository.count();
        long approuvees = decisionRepository.countByStatutValidation(StatutDecisionEnum.APPROUVEE);
        long modifiees = decisionRepository.countByStatutValidation(StatutDecisionEnum.MODIFIEE);
        long rejetees = decisionRepository.countByStatutValidation(StatutDecisionEnum.REJETEE);
        long enAttente = decisionRepository.countByStatutValidation(StatutDecisionEnum.EN_ATTENTE);
        long brouillon = decisionRepository.countByStatutValidation(StatutDecisionEnum.BROUILLON);

        double tauxValidation = total == 0 ? 0.0d
                : Math.round((approuvees * 1000.0d) / total) / 10.0d;

        List<OpenRouterAgentDefinition> agents = openRouterAgentRegistryService.configuredAgents();
        String agentsLabel = agents.stream()
                .map(OpenRouterAgentDefinition::displayName)
                .collect(Collectors.joining(" · "));

        List<ComparaisonAgentResponse> agentPerformance = comparaisonService.classerAgentsOpenRouter();
        List<DashboardResponse.RecentDecisionSummary> recent = decisionRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(0, RECENT_LIMIT))
                .stream()
                .map(this::toRecentSummary)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalDecisions(total)
                .approuvees(approuvees)
                .modifiees(modifiees)
                .rejetees(rejetees)
                .enAttente(enAttente)
                .brouillon(brouillon)
                .tauxValidation(tauxValidation)
                .agentsActifs(agents.size())
                .agentsLabel(agentsLabel)
                .hashChainIntact(hashChainService.verifierIntegrite())
                .generatedAt(LocalDateTime.now())
                .recentDecisions(recent)
                .agentPerformance(agentPerformance)
                .build();
    }

    private DashboardResponse.RecentDecisionSummary toRecentSummary(Decision decision) {
        String agentLabel = decision.getModelName();
        if (decision.getReponsesAgents() != null && !decision.getReponsesAgents().isEmpty()) {
            agentLabel = "ML + OpenRouter";
        } else if (decision.getSystemeIa() != null) {
            agentLabel = decision.getSystemeIa().getNom();
        }

        return DashboardResponse.RecentDecisionSummary.builder()
                .decisionId(decision.getDecisionId())
                .prompt(decision.getPrompt())
                .modelName(decision.getModelName())
                .agentLabel(agentLabel)
                .statutValidation(decision.getStatutValidation())
                .timestamp(decision.getTimestamp())
                .build();
    }
}
