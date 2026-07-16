package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.DecisionSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionSourceRepository extends JpaRepository<DecisionSource, UUID> {

    List<DecisionSource> findByDecisionDecisionIdOrderByCreatedAtAsc(UUID decisionId);

    Optional<DecisionSource> findBySourceIdAndDecisionDecisionId(UUID sourceId, UUID decisionId);

    long countByDecisionDecisionId(UUID decisionId);
}
