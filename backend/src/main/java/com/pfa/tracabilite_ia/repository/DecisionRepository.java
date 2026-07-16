package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, UUID> {

    @Query("""
            SELECT d FROM Decision d
            LEFT JOIN FETCH d.explanationFactors
            WHERE d.decisionId = :id
            """)
    Optional<Decision> findByIdWithFactors(@Param("id") UUID id);

    @Query("""
            SELECT d FROM Decision d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(d.prompt) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.contexte) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.suggestedDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:statut IS NULL OR d.statutValidation = :statut)
            """)
    Page<Decision> search(@Param("search") String search,
                          @Param("statut") StatutDecisionEnum statut,
                          Pageable pageable);

    @Query("""
            select count(d)
            from Decision d
            where d.systemeIa.systemeIaId = :systemeIaId
               or lower(d.modelName) = lower(:nomSysteme)
            """)
    long countBySystemeIaOuNom(@Param("systemeIaId") UUID systemeIaId,
                               @Param("nomSysteme") String nomSysteme);

    @Query("""
            select count(d)
            from Decision d
            where (d.systemeIa.systemeIaId = :systemeIaId
               or lower(d.modelName) = lower(:nomSysteme))
              and d.statutValidation = :statut
            """)
    long countBySystemeIaOuNomAndStatut(@Param("systemeIaId") UUID systemeIaId,
                                        @Param("nomSysteme") String nomSysteme,
                                        @Param("statut") StatutDecisionEnum statut);

    long countByStatutValidation(StatutDecisionEnum statut);

    List<Decision> findAllByOrderByTimestampDesc(Pageable pageable);

    List<Decision> findAllByOrderByTimestampAsc();

    java.util.Optional<Decision> findTopByDecisionIdNotOrderByTimestampDesc(UUID decisionId);
}
