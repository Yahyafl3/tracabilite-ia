package com.pfa.tracabilite_ia.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroqRequest {

    private String model;
    private List<GroqMessage> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    public GroqRequest() {
    }

    public GroqRequest(String model, List<GroqMessage> messages, ResponseFormat responseFormat) {
        this.model = model;
        this.messages = messages;
        this.responseFormat = responseFormat;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<GroqMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<GroqMessage> messages) {
        this.messages = messages;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
    }

    public record ResponseFormat(String type) {
    }
}
