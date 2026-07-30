package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.EducationDecisionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EducationDecisionDataRepository extends JpaRepository<EducationDecisionData, UUID> {
    Optional<EducationDecisionData> findByDecision_DecisionId(UUID decisionId);
}
