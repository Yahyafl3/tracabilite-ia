package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.DecisionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DecisionHistoryRepository extends JpaRepository<DecisionHistory, UUID> {

    List<DecisionHistory> findByDecisionDecisionIdOrderByCreatedAtAsc(UUID decisionId);
}
