package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.dto.response.ComparaisonAgentResponse;

import java.util.List;

public interface ComparaisonService {

    List<ComparaisonAgentResponse> classerAgents();
}