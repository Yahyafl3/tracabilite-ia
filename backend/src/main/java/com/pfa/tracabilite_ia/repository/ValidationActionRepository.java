package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.entities.ValidationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationActionRepository extends JpaRepository<ValidationAction, UUID> {

    List<ValidationAction> findByDecisionDecisionIdOrderByTimestampDesc(UUID decisionId);

    boolean existsByValidateur(Utilisateur validateur);
}
