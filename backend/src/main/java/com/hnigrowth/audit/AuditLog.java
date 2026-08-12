package com.hnigrowth.audit;

import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    private String entityType;
    private Long entityId;

    /** Who performed the action (email/username or "system"). */
    private String performedBy;

    @Column(length = 1000)
    private String details;
}
