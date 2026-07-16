package com.pfa.tracabilite_ia.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DecisionAnalysisRequest {

    @NotBlank(message = "Le prompt est obligatoire")
    private String prompt;

    private String contexte;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getContexte() {
        return contexte;
    }

    public void setContexte(String contexte) {
        this.contexte = contexte;
    }
}
