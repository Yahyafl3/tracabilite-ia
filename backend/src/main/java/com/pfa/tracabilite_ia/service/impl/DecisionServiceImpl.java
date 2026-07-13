package com.pfa.tracabilite_ia.service.impl;

import com.pfa.tracabilite_ia.dto.request.DecisionRequest;
import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.entities.SystemeIA;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;
import com.pfa.tracabilite_ia.repository.DecisionRepository;
import com.pfa.tracabilite_ia.repository.SystemeIARepository;
import com.pfa.tracabilite_ia.service.DecisionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DecisionServiceImpl implements DecisionService {

    private final DecisionRepository decisionRepository;
    private final SystemeIARepository systemeIARepository;

    public DecisionServiceImpl(DecisionRepository decisionRepository,
                              SystemeIARepository systemeIARepository) {
        this.decisionRepository = decisionRepository;
        this.systemeIARepository = systemeIARepository;
    }

    @Override
    public Decision creer(DecisionRequest request) {
        Decision decision = mapToEntity(request, new Decision());
        if (decision.getStatutValidation() == null) {
            decision.setStatutValidation(StatutDecisionEnum.EN_ATTENTE);
        }

        Decision saved = decisionRepository.save(decision);
        saved.setCurrentHash(saved.calculerHash());
        return decisionRepository.save(saved);
    }

    @Override
    public List<Decision> lister() {
        return decisionRepository.findAll();
    }

    @Override
    public Decision obtenir(UUID id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + id));
    }

    @Override
    public Decision mettreAJour(UUID id, DecisionRequest request) {
        Decision decision = obtenir(id);
        mapToEntity(request, decision);
        decision.setCurrentHash(decision.calculerHash());
        return decisionRepository.save(decision);
    }

    private Decision mapToEntity(DecisionRequest request, Decision decision) {
        decision.setPrompt(request.getPrompt());
        decision.setContexte(request.getContexte());
        decision.setModelName(request.getModelName());
        decision.setModelVersion(request.getModelVersion());
        decision.setReponse(request.getReponse());

        if (request.getSystemeIaId() != null) {
            SystemeIA systemeIA = systemeIARepository.findById(request.getSystemeIaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Systeme IA introuvable : " + request.getSystemeIaId()));
            decision.setSystemeIa(systemeIA);
        }

        if (request.getStatutValidation() != null) {
            decision.setStatutValidation(request.getStatutValidation());
        }

        return decision;
    }
}
