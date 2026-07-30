package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.MedicalDecisionData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MedicalDecisionDataRepository extends JpaRepository<MedicalDecisionData, UUID> {
    Optional<MedicalDecisionData> findByDecision_DecisionId(UUID decisionId);
}
