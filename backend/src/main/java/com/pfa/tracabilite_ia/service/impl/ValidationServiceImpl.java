package com.pfa.tracabilite_ia.service.impl;



import com.pfa.tracabilite_ia.dto.request.ValidationRequest;

import com.pfa.tracabilite_ia.dto.response.DecisionPageResponse;

import com.pfa.tracabilite_ia.dto.response.DecisionResponse;

import com.pfa.tracabilite_ia.dto.response.ValidationActionResponse;

import com.pfa.tracabilite_ia.entities.Decision;

import com.pfa.tracabilite_ia.entities.Utilisateur;

import com.pfa.tracabilite_ia.entities.ValidationAction;

import com.pfa.tracabilite_ia.enumeration.DecisionHistoryAction;

import com.pfa.tracabilite_ia.enumeration.RoleEnum;

import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;

import com.pfa.tracabilite_ia.enumeration.TypeActionEnum;

import com.pfa.tracabilite_ia.exception.ResourceNotFoundException;

import com.pfa.tracabilite_ia.exception.UnauthorizedActionException;

import com.pfa.tracabilite_ia.mapper.DecisionMapper;

import com.pfa.tracabilite_ia.mapper.ValidationMapper;

import com.pfa.tracabilite_ia.repository.DecisionRepository;

import com.pfa.tracabilite_ia.repository.ValidationActionRepository;

import com.pfa.tracabilite_ia.service.AuthService;

import com.pfa.tracabilite_ia.service.DecisionHashService;

import com.pfa.tracabilite_ia.service.DecisionHistoryService;

import com.pfa.tracabilite_ia.service.ValidationService;

import org.hibernate.Hibernate;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.HashMap;

import java.util.List;

import java.util.Map;

import java.util.Set;

import java.util.UUID;



@Service

public class ValidationServiceImpl implements ValidationService {



    private static final Set<String> DECISIONS_HUMAINES = Set.of("APPROUVER", "REJETER");



    private final DecisionRepository decisionRepository;

    private final ValidationActionRepository validationActionRepository;

    private final AuthService authService;

    private final DecisionMapper decisionMapper;

    private final ValidationMapper validationMapper;

    private final DecisionHistoryService decisionHistoryService;

    private final DecisionHashService decisionHashService;



    public ValidationServiceImpl(DecisionRepository decisionRepository,

                                 ValidationActionRepository validationActionRepository,

                                 AuthService authService,

                                 DecisionMapper decisionMapper,

                                 ValidationMapper validationMapper,

                                 DecisionHistoryService decisionHistoryService,

                                 DecisionHashService decisionHashService) {

        this.decisionRepository = decisionRepository;

        this.validationActionRepository = validationActionRepository;

        this.authService = authService;

        this.decisionMapper = decisionMapper;

        this.validationMapper = validationMapper;

        this.decisionHistoryService = decisionHistoryService;

        this.decisionHashService = decisionHashService;

    }



    @Override

    @Transactional(readOnly = true)

    public DecisionPageResponse listerEnAttente(int page, int size) {

        assertValidateur();

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Decision> result = decisionRepository.search(null, StatutDecisionEnum.EN_ATTENTE, pageable);

        return DecisionPageResponse.builder()

                .content(decisionMapper.toResponseList(result.getContent()))

                .page(result.getNumber())

                .size(result.getSize())

                .totalElements(result.getTotalElements())

                .totalPages(result.getTotalPages())

                .last(result.isLast())

                .build();

    }



    @Override

    @Transactional(readOnly = true)

    public List<ValidationActionResponse> historique(UUID decisionId) {

        decisionRepository.findById(decisionId)

                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));

        return validationMapper.toResponseList(

                validationActionRepository.findByDecisionDecisionIdOrderByTimestampDesc(decisionId));

    }



    @Override

    @Transactional

    public DecisionResponse approuver(UUID decisionId, ValidationRequest request) {

        return valider(decisionId, TypeActionEnum.APPROUVER, StatutDecisionEnum.APPROUVEE,

                "APPROUVER", DecisionHistoryAction.DECISION_APPROVED, request);

    }



    @Override

    @Transactional

    public DecisionResponse rejeter(UUID decisionId, ValidationRequest request) {

        return valider(decisionId, TypeActionEnum.REJETER, StatutDecisionEnum.REJETEE,

                "REJETER", DecisionHistoryAction.DECISION_REJECTED, request);

    }



    @Override

    @Transactional

    public DecisionResponse modifier(UUID decisionId, ValidationRequest request) {

        String decisionHumaine = normalizeDecisionHumaine(request != null ? request.getDecisionHumaine() : null);

        if (decisionHumaine == null) {

            throw new IllegalArgumentException("La décision humaine est requise pour une modification (APPROUVER ou REJETER).");

        }

        return valider(decisionId, TypeActionEnum.MODIFIER, StatutDecisionEnum.MODIFIEE,

                decisionHumaine, DecisionHistoryAction.DECISION_MODIFIED, request);

    }



    private DecisionResponse valider(UUID decisionId,

                                     TypeActionEnum typeAction,

                                     StatutDecisionEnum statutApres,

                                     String decisionHumaine,

                                     DecisionHistoryAction historyAction,

                                     ValidationRequest request) {

        Utilisateur validateur = assertValidateur();

        Decision decision = decisionRepository.findByIdWithFactors(decisionId)

                .orElseThrow(() -> new ResourceNotFoundException("Decision introuvable : " + decisionId));

        Hibernate.initialize(decision.getReponsesAgents());



        if (decision.getStatutValidation() != StatutDecisionEnum.EN_ATTENTE) {

            throw new IllegalStateException(

                    "Seules les décisions EN_ATTENTE peuvent être validées (statut actuel : "

                            + decision.getStatutValidation() + ").");

        }



        StatutDecisionEnum statutAvant = decision.getStatutValidation();

        decision.changerStatut(statutApres);

        decision.setHumanDecision(decisionHumaine);

        decision.setValidatorEmail(validateur.getEmail());

        decisionHashService.refreshHashComponents(decision, decision.getReponsesAgents());

        Decision saved = decisionRepository.save(decision);



        ValidationAction action = new ValidationAction();

        action.setDecision(saved);

        action.setValidateur(validateur);

        action.setTypeAction(typeAction);

        action.setStatutAvant(statutAvant);

        action.setStatutApres(statutApres);

        action.setDecisionHumaine(decisionHumaine);

        action.setCommentaire(request != null ? request.getCommentaire() : null);

        validationActionRepository.save(action);



        Map<String, Object> eventData = new HashMap<>();

        eventData.put("decisionHumaine", decisionHumaine);

        eventData.put("typeAction", typeAction.name());

        if (request != null && request.getCommentaire() != null) {

            eventData.put("commentaire", request.getCommentaire());

        }

        decisionHistoryService.record(saved, historyAction, statutAvant, statutApres,

                validateur.getId(), validateur.getEmail(),

                request != null ? request.getCommentaire() : null, null, eventData);



        DecisionResponse response = decisionMapper.toResponse(saved);

        response.setValidations(validationMapper.toResponseList(

                validationActionRepository.findByDecisionDecisionIdOrderByTimestampDesc(decisionId)));

        return response;

    }



    private Utilisateur assertValidateur() {

        Utilisateur user = authService.getCurrentUser();

        RoleEnum role = user.getRole();

        if (role != RoleEnum.VALIDATEUR && role != RoleEnum.ADMINISTRATEUR) {

            throw new UnauthorizedActionException(

                    "Seuls les validateurs et administrateurs peuvent effectuer cette action.");

        }

        return user;

    }



    private String normalizeDecisionHumaine(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        String normalized = value.trim().toUpperCase();

        if (!DECISIONS_HUMAINES.contains(normalized)) {

            throw new IllegalArgumentException("Décision humaine invalide : " + value + " (APPROUVER ou REJETER attendu).");

        }

        return normalized;

    }

}

