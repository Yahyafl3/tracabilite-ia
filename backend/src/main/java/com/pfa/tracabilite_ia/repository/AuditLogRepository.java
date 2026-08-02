package com.pfa.tracabilite_ia.repository;

import com.pfa.tracabilite_ia.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByDecisionIdOrderByTimestampDesc(UUID decisionId);
}
