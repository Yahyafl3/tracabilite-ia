package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.SystemeIA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemeIARepository extends JpaRepository<SystemeIA, UUID> {

    List<SystemeIA> findAllByActifTrueOrderByNomAsc();

    Optional<SystemeIA> findByNomIgnoreCaseAndFournisseurIgnoreCase(String nom, String fournisseur);

    boolean existsByNomIgnoreCaseAndFournisseurIgnoreCase(String nom, String fournisseur);
}