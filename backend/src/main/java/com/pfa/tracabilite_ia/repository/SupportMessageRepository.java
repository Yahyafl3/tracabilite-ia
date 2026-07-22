package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.SupportMessage;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.SupportMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {

    @Query("""
            SELECT m FROM SupportMessage m
            WHERE (:status IS NULL OR m.status = :status)
              AND (
                :query IS NULL OR :query = '' OR
                LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
                LOWER(m.subject) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<SupportMessage> search(
            @Param("status") SupportMessageStatus status,
            @Param("query") String query,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("update SupportMessage m set m.processedBy = null where m.processedBy = :utilisateur")
    int clearProcessedBy(@Param("utilisateur") Utilisateur utilisateur);
}
