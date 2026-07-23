package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.AppelIA;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AppelIARepository extends JpaRepository<AppelIA, UUID> {

    @Modifying(clearAutomatically = true)
    @Query("update AppelIA a set a.utilisateur = null where a.utilisateur = :utilisateur")
    int clearUtilisateur(@Param("utilisateur") Utilisateur utilisateur);
}
