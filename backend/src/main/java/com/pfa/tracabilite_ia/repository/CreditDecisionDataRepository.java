package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.CreditDecisionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditDecisionDataRepository extends JpaRepository<CreditDecisionData, UUID> {
    Optional<CreditDecisionData> findByDecision_DecisionId(UUID decisionId);
}
