package com.pfa.tracabilite_ia.ai.dto;

import java.util.List;

public class OpenRouterChatResult {

    private String rawContent;
    private OpenRouterChatResponse response;
    private long durationMs;
    private int retryCount;
    private String requestedModelId;
    private String actualModelId;
    private boolean fallbackUsed;
    private String fallbackReason;
    private String responseHash;
    private List<String> modelsRequested;

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

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public String getRequestedModelId() {
        return requestedModelId;
    }

    public void setRequestedModelId(String requestedModelId) {
        this.requestedModelId = requestedModelId;
    }

    public String getActualModelId() {
        return actualModelId;
    }

    public void setActualModelId(String actualModelId) {
        this.actualModelId = actualModelId;
    }

    public boolean isFallbackUsed() {
        return fallbackUsed;
    }

    public void setFallbackUsed(boolean fallbackUsed) {
        this.fallbackUsed = fallbackUsed;
    }

    public String getFallbackReason() {
        return fallbackReason;
    }

    public void setFallbackReason(String fallbackReason) {
        this.fallbackReason = fallbackReason;
    }

    public String getResponseHash() {
        return responseHash;
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }

    public List<String> getModelsRequested() {
        return modelsRequested;
    }

    public void setModelsRequested(List<String> modelsRequested) {
        this.modelsRequested = modelsRequested;
    }

    public Integer getTotalTokens() {
        if (response == null || response.getUsage() == null) {
            return null;
        }
        return response.getUsage().getTotalTokens();
    }
}
