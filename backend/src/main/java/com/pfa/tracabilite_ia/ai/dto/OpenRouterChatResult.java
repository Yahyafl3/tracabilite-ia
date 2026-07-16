package com.pfa.tracabilite_ia.ai.dto;

public class OpenRouterChatResult {

    private String rawContent;
    private OpenRouterChatResponse response;
    private long durationMs;

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public OpenRouterChatResponse getResponse() {
        return response;
    }

    public void setResponse(OpenRouterChatResponse response) {
        this.response = response;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public Integer getTotalTokens() {
        if (response == null || response.getUsage() == null) {
            return null;
        }
        return response.getUsage().getTotalTokens();
    }
}
