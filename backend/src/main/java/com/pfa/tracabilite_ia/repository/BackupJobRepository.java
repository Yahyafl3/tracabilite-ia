package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.BackupJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BackupJobRepository extends JpaRepository<BackupJob, UUID> {

    List<BackupJob> findAllByOrderByCreatedAtDesc();
}
