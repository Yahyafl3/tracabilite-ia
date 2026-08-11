package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.DecisionDomain;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
                   LOWER(COALESCE(d.suggestedDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.dossierReference, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:statut IS NULL OR d.statutValidation = :statut)
            """)
    Page<Decision> search(@Param("search") String search,
                          @Param("statut") StatutDecisionEnum statut,
                          Pageable pageable);

    @Query("""
            SELECT d FROM Decision d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(d.prompt) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.contexte) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.suggestedDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.dossierReference, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.humanDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:statut IS NULL OR d.statutValidation = :statut)
              AND (
                    :domaine IS NULL
                    OR d.domaine = :domaine
                    OR (:domaine = com.pfa.tracabilite_ia.enumeration.DecisionDomain.CREDIT AND d.domaine IS NULL)
                  )
              AND (:riskLevel IS NULL OR :riskLevel = '' OR UPPER(COALESCE(d.riskLevel, '')) = UPPER(:riskLevel))
              AND (:decisionFinale IS NULL OR :decisionFinale = '' OR UPPER(COALESCE(d.humanDecision, '')) = UPPER(:decisionFinale))
              AND (
                    :validateur IS NULL OR :validateur = ''
                    OR LOWER(COALESCE(d.validatorEmail, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                    OR LOWER(COALESCE(d.validateurRole, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                  )
              AND d.timestamp >= :fromDate
              AND d.timestamp <= :toDate
            """)
    Page<Decision> searchFiltered(
            @Param("search") String search,
            @Param("statut") StatutDecisionEnum statut,
            @Param("domaine") DecisionDomain domaine,
            @Param("riskLevel") String riskLevel,
            @Param("decisionFinale") String decisionFinale,
            @Param("validateur") String validateur,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query("""
            SELECT d FROM Decision d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(d.prompt) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.contexte) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.suggestedDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.dossierReference, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.humanDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:statut IS NULL OR d.statutValidation = :statut)
              AND (
                    :domaine IS NULL
                    OR d.domaine = :domaine
                    OR (:domaine = com.pfa.tracabilite_ia.enumeration.DecisionDomain.CREDIT AND d.domaine IS NULL)
                  )
              AND (:riskLevel IS NULL OR :riskLevel = '' OR UPPER(COALESCE(d.riskLevel, '')) = UPPER(:riskLevel))
              AND (:decisionFinale IS NULL OR :decisionFinale = '' OR UPPER(COALESCE(d.humanDecision, '')) = UPPER(:decisionFinale))
              AND (
                    :validateur IS NULL OR :validateur = ''
                    OR LOWER(COALESCE(d.validatorEmail, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                    OR LOWER(COALESCE(d.validateurRole, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                  )
              AND d.timestamp >= :fromDate
              AND d.timestamp <= :toDate
              AND (:createdBy = '' OR LOWER(COALESCE(d.createdBy, '')) = LOWER(:createdBy))
            """)
    Page<Decision> searchFilteredWithCreator(
            @Param("search") String search,
            @Param("statut") StatutDecisionEnum statut,
            @Param("domaine") DecisionDomain domaine,
            @Param("riskLevel") String riskLevel,
            @Param("decisionFinale") String decisionFinale,
            @Param("validateur") String validateur,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("createdBy") String createdBy,
            Pageable pageable
    );

    /**
     * Search for domain agents: shows own decisions + decisions created by ADMINISTRATEUR in their domain
     */
    @Query("""
            SELECT d FROM Decision d
            LEFT JOIN Utilisateur u ON LOWER(d.createdBy) = LOWER(u.email)
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(d.prompt) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.contexte) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.suggestedDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.dossierReference, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(d.humanDecision, '')) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:statut IS NULL OR d.statutValidation = :statut)
              AND (
                    :domaine IS NULL
                    OR d.domaine = :domaine
                    OR (:domaine = com.pfa.tracabilite_ia.enumeration.DecisionDomain.CREDIT AND d.domaine IS NULL)
                  )
              AND (:riskLevel IS NULL OR :riskLevel = '' OR UPPER(COALESCE(d.riskLevel, '')) = UPPER(:riskLevel))
              AND (:decisionFinale IS NULL OR :decisionFinale = '' OR UPPER(COALESCE(d.humanDecision, '')) = UPPER(:decisionFinale))
              AND (
                    :validateur IS NULL OR :validateur = ''
                    OR LOWER(COALESCE(d.validatorEmail, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                    OR LOWER(COALESCE(d.validateurRole, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                  )
              AND d.timestamp >= :fromDate
              AND d.timestamp <= :toDate
              AND (
                    LOWER(COALESCE(d.createdBy, '')) = LOWER(:createdBy)
                    OR u.role = com.pfa.tracabilite_ia.enumeration.RoleEnum.ADMINISTRATEUR
                  )
            """)
    Page<Decision> searchFilteredForDomainAgent(
            @Param("search") String search,
            @Param("statut") StatutDecisionEnum statut,
            @Param("domaine") DecisionDomain domaine,
            @Param("riskLevel") String riskLevel,
            @Param("decisionFinale") String decisionFinale,
            @Param("validateur") String validateur,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("createdBy") String createdBy,
            Pageable pageable
    );

    /**
     * Dates toujours bornées côté service (évite PKIX/null typing PostgreSQL sur {@code ? IS NULL} timestamp).
     */
    @Query("""
            SELECT d FROM Decision d
            WHERE (:domaine IS NULL
                    OR d.domaine = :domaine
                    OR (:domaine = com.pfa.tracabilite_ia.enumeration.DecisionDomain.CREDIT AND d.domaine IS NULL))
              AND (:statut IS NULL OR d.statutValidation = :statut)
              AND d.timestamp >= :fromDate
              AND d.timestamp <= :toDate
              AND (
                    :validateur = ''
                    OR LOWER(COALESCE(d.validatorEmail, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                    OR LOWER(COALESCE(d.validateurRole, '')) LIKE LOWER(CONCAT('%', :validateur, '%'))
                  )
            ORDER BY d.timestamp DESC
            """)
    List<Decision> findForExport(
            @Param("domaine") DecisionDomain domaine,
            @Param("statut") StatutDecisionEnum statut,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("validateur") String validateur
    );

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

    List<Decision> findByStatutValidationInOrderByTimestampDesc(List<StatutDecisionEnum> statuts);
}
