package com.pfa.tracabilite_ia.ai.service;

import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String systemPromptForDecisionAnalysis() {
        return """
                Tu es un assistant d'analyse de decisions pour un systeme de tracabilite IA.
                Reponds uniquement en JSON valide, sans markdown, sans texte autour.
                Le JSON doit respecter exactement ce schema :
                {
                  "suggestedDecision": "APPROVE|REJECT|REVIEW",
                  "confidence": 0.0,
                  "riskLevel": "LOW|MEDIUM|HIGH",
                  "summary": "resume court du contexte",
                  "explanation": "explication de la recommandation",
                  "recommendations": ["action 1", "action 2"]
                }
                """;
    }

    public String userPromptForDecisionAnalysis(String prompt, String contexte) {
        StringBuilder builder = new StringBuilder();
        builder.append("Analyse la decision suivante et produis une recommandation structuree.\n\n");
        builder.append("Prompt metier:\n").append(prompt).append("\n\n");
        if (contexte != null && !contexte.isBlank()) {
            builder.append("Contexte:\n").append(contexte).append("\n");
        }
        return builder.toString();
    }

    public String systemPromptForSummary() {
        return "Tu resumes un contexte metier en francais, en 3 phrases maximum, sans donnees sensibles.";
    }

    public String systemPromptForRisk() {
        return "Tu evalues le niveau de risque (LOW, MEDIUM, HIGH) et expliques brievement pourquoi.";
    }

    public String systemPromptForRecommendation() {
        return "Tu proposes une recommandation actionnable en une phrase claire.";
    }

    public String systemPromptForExplanation() {
        return "Tu expliques une decision IA de maniere comprehensible pour un validateur humain.";
    }
}
