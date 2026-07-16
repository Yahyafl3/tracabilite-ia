package com.pfa.tracabilite_ia.mapper;

import com.pfa.tracabilite_ia.dto.response.ValidationActionResponse;
import com.pfa.tracabilite_ia.entities.ValidationAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ValidationMapper {

    public ValidationActionResponse toResponse(ValidationAction action) {
        return ValidationActionResponse.builder()
                .validationActionId(action.getValidationActionId())
                .decisionId(action.getDecision().getDecisionId())
                .validateurId(action.getValidateur().getId())
                .validateurNom(action.getValidateur().getNom())
                .typeAction(action.getTypeAction())
                .statutAvant(action.getStatutAvant())
                .statutApres(action.getStatutApres())
                .decisionHumaine(action.getDecisionHumaine())
                .commentaire(action.getCommentaire())
                .timestamp(action.getTimestamp())
                .build();
    }

    public List<ValidationActionResponse> toResponseList(List<ValidationAction> actions) {
        return actions.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
