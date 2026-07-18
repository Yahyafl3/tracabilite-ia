package com.pfa.tracabilite_ia.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenRouterChatRequest {

    private String model;
    private List<String> models;
    private List<Message> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    public OpenRouterChatRequest() {
    }

    public OpenRouterChatRequest(String model, List<String> models, List<Message> messages, ResponseFormat responseFormat) {
        this.model = model;
        this.models = models;
        this.messages = messages;
        this.responseFormat = responseFormat;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    public record Message(String role, String content) {
    }

    public record ResponseFormat(String type) {
    }
}
