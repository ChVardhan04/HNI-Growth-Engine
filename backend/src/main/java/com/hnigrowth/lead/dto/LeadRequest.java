package com.hnigrowth.lead.dto;

import com.hnigrowth.lead.LeadPriority;
import com.hnigrowth.lead.LeadSource;
import jakarta.validation.constraints.NotBlank;

/** Inbound payload for creating/updating a lead. AI fields are never client-supplied. */
public record LeadRequest(
        @NotBlank String name,
        String email,
        String phone,
        String company,
        String designation,
        String industry,
        String location,
        Integer yearsExperience,
        String linkedinUrl,
        LeadSource source,
        LeadPriority priority,
        String notes,
        Integer previousEngagementScore
) {}
