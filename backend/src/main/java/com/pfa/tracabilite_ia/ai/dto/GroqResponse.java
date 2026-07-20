package com.pfa.tracabilite_ia.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroqResponse {

    private String id;
    private String model;
    private List<GroqChoice> choices;
    private GroqUsage usage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<GroqChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<GroqChoice> choices) {
        this.choices = choices;
    }

    public GroqUsage getUsage() {
        return usage;
    }

    public void setUsage(GroqUsage usage) {
        this.usage = usage;
    }
}
