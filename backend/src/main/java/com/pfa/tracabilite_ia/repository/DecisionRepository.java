package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, UUID> {
}
