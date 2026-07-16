package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.ReponseAgentIA;
import com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReponseAgentIARepository extends JpaRepository<ReponseAgentIA, UUID> {

    List<ReponseAgentIA> findByDecisionDecisionIdOrderByAgentKeyAsc(UUID decisionId);

    long countByAgentKey(String agentKey);

    long countByAgentKeyAndStatut(String agentKey, StatutReponseAgentEnum statut);

    @Query("""
            SELECT COUNT(r) FROM ReponseAgentIA r
            WHERE r.agentKey = :agentKey
              AND r.statut = com.pfa.tracabilite_ia.enumeration.StatutReponseAgentEnum.SUCCESS
              AND r.decisionProposee = :decision
            """)
    long countSuccessfulByAgentKeyAndDecision(@Param("agentKey") String agentKey,
                                              @Param("decision") String decision);
}
