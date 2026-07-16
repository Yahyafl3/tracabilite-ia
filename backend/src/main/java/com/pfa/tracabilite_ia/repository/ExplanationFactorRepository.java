package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.ExplanationFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExplanationFactorRepository extends JpaRepository<ExplanationFactor, UUID> {
}
