package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.Decision;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, UUID> {

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
}
