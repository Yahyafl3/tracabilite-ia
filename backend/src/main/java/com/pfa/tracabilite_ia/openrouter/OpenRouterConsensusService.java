package com.pfa.tracabilite_ia.openrouter;

import com.pfa.tracabilite_ia.dto.response.ConsensusResponse;
import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenRouterConsensusService {

    public ConsensusResponse compute(List<ReponseAgentIA> responses) {
        List<ReponseAgentIA> successful = responses.stream()
                .filter(response -> response.getStatut() == StatutReponseAgentEnum.SUCCESS)
                .toList();

        Map<String, Integer> votes = new HashMap<>();
        double confidenceSum = 0.0;
        int confidenceCount = 0;

        for (ReponseAgentIA response : successful) {
            String vote = normalizeVote(response.getDecisionProposee());
            if (vote != null) {
                votes.merge(vote, 1, Integer::sum);
            }
            if (response.getConfianceDeclaree() != null) {
                confidenceSum += response.getConfianceDeclaree();
                confidenceCount++;
            }
        }

        int successfulCount = successful.size();
        ConsensusOutcome outcome = determineConsensus(successfulCount, votes);

        String resume = successful.stream()
                .map(ReponseAgentIA::getResume)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" | "));

        return ConsensusResponse.builder()
                .decisionConsensus(outcome.decision())
                .confianceMoyenne(confidenceCount > 0 ? confidenceSum / confidenceCount : null)
                .agentsConsultes(responses.size())
                .agentsReussis(successfulCount)
                .successfulAgentCount(successfulCount)
                .votes(votes)
                .agreementRate(outcome.agreementRate())
                .consensusAvailable(outcome.consensusAvailable())
                .resume(resume.isBlank() ? null : resume)
                .note("Consensus informatif uniquement. La decision ML LogisticRegression et les SHAP ne sont pas modifies.")
                .build();
    }

    public ConsensusResponse buildSkippedConsensus(String message) {
        return ConsensusResponse.builder()
                .decisionConsensus("INSUFFICIENT_RESPONSES")
                .agentsConsultes(0)
                .agentsReussis(0)
                .successfulAgentCount(0)
                .consensusAvailable(false)
                .resume(message)
                .note(message)
                .build();
    }

    private ConsensusOutcome determineConsensus(int successfulCount, Map<String, Integer> votes) {
        if (successfulCount <= 1) {
            return new ConsensusOutcome("INSUFFICIENT_RESPONSES", null, false);
        }

        if (successfulCount == 2) {
            if (votes.size() == 1) {
                return new ConsensusOutcome(votes.keySet().iterator().next(), 100.0, true);
            }
            return new ConsensusOutcome("NO_CONSENSUS", 50.0, false);
        }

        int maxVotes = votes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (votes.size() == 3) {
            return new ConsensusOutcome("NO_CONSENSUS", 33.33, false);
        }
        if (maxVotes >= 2) {
            String majorityDecision = votes.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxVotes)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("NO_CONSENSUS");
            if (maxVotes == 3) {
                return new ConsensusOutcome(majorityDecision, 100.0, true);
            }
            return new ConsensusOutcome(majorityDecision, 66.67, true);
        }

        return new ConsensusOutcome("NO_CONSENSUS", 33.33, false);
    }

    private String normalizeVote(String decision) {
        if (decision == null || decision.isBlank()) {
            return null;
        }
        String normalized = decision.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "APPROUVER", "APPROVE" -> "APPROUVER";
            case "REJETER", "REJECT" -> "REJETER";
            case "REVIEW" -> "REVIEW";
            default -> normalized;
        };
    }

    private record ConsensusOutcome(String decision, Double agreementRate, boolean consensusAvailable) {
    }
}
