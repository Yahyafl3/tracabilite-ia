package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.PasswordResetToken;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("""
            update PasswordResetToken t
            set t.used = true, t.usedAt = :now
            where t.utilisateur = :utilisateur
              and t.used = false
            """)
    int invalidateActiveTokens(@Param("utilisateur") Utilisateur utilisateur,
                               @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("delete from PasswordResetToken t where t.utilisateur = :utilisateur")
    int deleteByUtilisateur(@Param("utilisateur") Utilisateur utilisateur);
}
