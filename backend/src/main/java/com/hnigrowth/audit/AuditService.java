package com.hnigrowth.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Single entry point for recording audit events. */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    public void log(AuditAction action, String entityType, Long entityId, String details) {
        log(action, entityType, entityId, currentUser(), details);
    }

    public void log(AuditAction action, String entityType, Long entityId, String performedBy, String details) {
        auditRepository.save(AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .build());
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }
}
