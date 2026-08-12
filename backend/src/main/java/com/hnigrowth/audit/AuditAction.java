package com.hnigrowth.audit;

/** Enumerates every auditable action across the platform. */
public enum AuditAction {
    CREATE, UPDATE, DELETE,
    APPROVAL, REJECTION,
    LOGIN, LOGOUT,
    AI_PROCESSING,
    CAMPAIGN,
    CRM,
    MEETING,
    INTENT_UPDATE,
    OUTREACH_SENT,
    ADVISOR_ROUTED
}
