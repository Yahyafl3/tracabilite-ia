package com.pfa.tracabilite_ia.ai.dto;

public class GroqChatResult {

    private String rawContent;
    private GroqResponse response;
    private long durationMs;
    private int retryCount;
    private String requestedModelId;
    private String actualModelId;
    private String responseHash;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public GroqResponse getResponse() {
        return response;
    }

    public void setResponse(GroqResponse response) {
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

    public String getResponseHash() {
        return responseHash;
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getCompletionTokens() {
        return completionTokens;
    }

    public void setCompletionTokens(Integer completionTokens) {
        this.completionTokens = completionTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }
}
