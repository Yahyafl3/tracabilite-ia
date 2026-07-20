package com.pfa.tracabilite_ia.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroqChoice {

    private GroqMessage message;
    private Integer index;
    private String finishReason;

    public GroqMessage getMessage() {
        return message;
    }

    public void setMessage(GroqMessage message) {
        this.message = message;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}
