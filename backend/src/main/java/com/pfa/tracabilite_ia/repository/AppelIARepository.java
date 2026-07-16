package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.AppelIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppelIARepository extends JpaRepository<AppelIA, UUID> {
}
