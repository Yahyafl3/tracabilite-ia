package com.pfa.tracabilite_ia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterProperties {

    private String baseUrl = "https://openrouter.ai/api/v1";
    private String apiKey = "";
    private String model1 = "meta-llama/llama-3.3-70b-instruct:free";
    private String model2 = "google/gemma-4-26b-a4b-it:free";
    private String model3 = "openai/gpt-oss-20b:free";
    private String agent1Fallback = "";
    private String agent2Fallback = "";
    private String agent3Fallback = "";
    private String httpReferer = "http://localhost";
    private String appTitle = "Tracabilite-IA";
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 120000;
    private int agentDelayMs = 3000;
    private long modelsCacheTtlMs = 300_000L;
    private boolean useFreeRouter = false;

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel1() {
        return model1;
    }

    public void setModel1(String model1) {
        this.model1 = model1;
    }

    public String getModel2() {
        return model2;
    }

    public void setModel2(String model2) {
        this.model2 = model2;
    }

    public String getModel3() {
        return model3;
    }

    public void setModel3(String model3) {
        this.model3 = model3;
    }

    public String getAgent1Fallback() {
        return agent1Fallback;
    }

    public void setAgent1Fallback(String agent1Fallback) {
        this.agent1Fallback = agent1Fallback;
    }

    public String getAgent2Fallback() {
        return agent2Fallback;
    }

    public void setAgent2Fallback(String agent2Fallback) {
        this.agent2Fallback = agent2Fallback;
    }

    public String getAgent3Fallback() {
        return agent3Fallback;
    }

    public void setAgent3Fallback(String agent3Fallback) {
        this.agent3Fallback = agent3Fallback;
    }

    public String fallbackForAgentKey(String agentKey) {
        return switch (agentKey) {
            case "AGENT_1" -> agent1Fallback;
            case "AGENT_2" -> agent2Fallback;
            case "AGENT_3" -> agent3Fallback;
            default -> "";
        };
    }

    public String getHttpReferer() {
        return httpReferer;
    }

    public void setHttpReferer(String httpReferer) {
        this.httpReferer = httpReferer;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getAgentDelayMs() {
        return agentDelayMs;
    }

    public void setAgentDelayMs(int agentDelayMs) {
        this.agentDelayMs = agentDelayMs;
    }

    public long getModelsCacheTtlMs() {
        return modelsCacheTtlMs;
    }

    public void setModelsCacheTtlMs(long modelsCacheTtlMs) {
        this.modelsCacheTtlMs = modelsCacheTtlMs;
    }

    public boolean isUseFreeRouter() {
        return useFreeRouter;
    }

    public void setUseFreeRouter(boolean useFreeRouter) {
        this.useFreeRouter = useFreeRouter;
    }
}
