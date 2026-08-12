package com.hnigrowth.lead.dto;

import com.hnigrowth.ai.model.RecommendationType;
import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.LeadPriority;
import com.hnigrowth.lead.LeadSource;
import com.hnigrowth.lead.LeadStatus;
import com.hnigrowth.lead.Tier;

import java.time.Instant;

/** Outbound DTO — entities are never exposed directly. */
public record LeadResponse(
        Long id,
        String name,
        String email,
        String phone,
        String company,
        String designation,
        String industry,
        String location,
        Integer yearsExperience,
        String linkedinUrl,
        LeadSource source,
        LeadStatus status,
        LeadPriority priority,
        String owner,
        Long assignedRmId,
        String assignedRmName,
        String notes,
        Integer icpScore,
        Tier tier,
        String icpReason,
        Integer icpConfidence,
        int intentScore,
        IntentBand intentBand,
        RecommendationType nextBestAction,
        String recommendationReason,
        String profileImageUrl,
        String currentPosition,
        String companySize,
        String connectionDegree,
        String connectionStatus,
        String skills,
        String education,
        Long campaignId,
        Instant createdAt,
        Instant updatedAt
) {}
