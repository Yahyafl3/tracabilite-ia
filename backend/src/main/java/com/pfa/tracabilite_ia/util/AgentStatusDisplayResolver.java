package com.pfa.tracabilite_ia.util;

import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;

public final class AgentStatusDisplayResolver {

    private AgentStatusDisplayResolver() {
    }

    public static String resolve(ReponseAgentIA entity) {
        if (entity.getStatut() == StatutReponseAgentEnum.SUCCESS) {
            return "SUCCESS";
        }
        if (entity.getCodeErreur() != null) {
            return switch (entity.getCodeErreur()) {
                case "OPENROUTER_RATE_LIMITED" -> "RATE_LIMITED";
                case "MODEL_UNAVAILABLE" -> "MODEL_UNAVAILABLE";
                case "OPENROUTER_TIMEOUT" -> "TIMEOUT";
                case "OPENROUTER_INVALID_RESPONSE" -> "INVALID_RESPONSE";
                default -> entity.getStatut().name();
            };
        }
        return entity.getStatut().name();
    }
}
