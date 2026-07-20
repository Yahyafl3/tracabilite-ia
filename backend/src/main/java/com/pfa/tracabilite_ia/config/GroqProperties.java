package com.pfa.tracabilite_ia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "groq")
public class GroqProperties {

    private String baseUrl = "https://api.groq.com/openai/v1";
    private String apiKey = "";
    private String model1 = "llama-3.3-70b-versatile";
    private String model2 = "openai/gpt-oss-120b";
    private String model3 = "openai/gpt-oss-20b";
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 120_000;
    private int maxRetries = 2;
    private int agentDelayMs = 1_000;

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

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getAgentDelayMs() {
        return agentDelayMs;
    }

    public void setAgentDelayMs(int agentDelayMs) {
        this.agentDelayMs = agentDelayMs;
    }
}
