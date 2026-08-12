package com.hnigrowth.dashboard;

import com.hnigrowth.audit.AuditLog;
import com.hnigrowth.lead.dto.LeadResponse;

import java.util.List;
import java.util.Map;

/** Aggregated dashboard payload. Goes well beyond simple counts. */
public record DashboardStats(
        long totalLeads,
        long qualifiedLeads,
        long salesReadyLeads,
        long convertedLeads,
        long hotLeads,
        long coldLeads,
        long pendingApprovals,
        long activeCampaigns,
        double conversionRate,          // %
        double predictedConversionRate, // %
        double revenueForecast,         // currency units
        Map<String, Long> leadsByTier,
        Map<String, Long> leadsByIntentBand,
        Map<String, Long> leadsByStatus,
        List<LeadResponse> topProspects,
        List<AuditLog> recentAuditLogs
) {}
