package com.pfa.tracabilite_ia.service;

import com.pfa.tracabilite_ia.entities.AuditLog;
import com.pfa.tracabilite_ia.entities.Utilisateur;
import com.pfa.tracabilite_ia.enumeration.StatutDecisionEnum;
import com.pfa.tracabilite_ia.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog record(
            UUID decisionId,
            Utilisateur user,
            String action,
            StatutDecisionEnum oldStatus,
            StatutDecisionEnum newStatus,
            String details,
            String correlationId
    ) {
        AuditLog log = AuditLog.builder()
                .decisionId(decisionId)
                .userId(user != null ? user.getId() : null)
                .userRole(user != null && user.getRole() != null ? user.getRole().name() : null)
                .action(action)
                .oldStatus(oldStatus != null ? oldStatus.name() : null)
                .newStatus(newStatus != null ? newStatus.name() : null)
                .details(details)
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .build();
        return auditLogRepository.save(log);
    }
}
