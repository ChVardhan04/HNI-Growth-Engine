package com.hnigrowth.campaign.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

public record CampaignRequest(
        @NotBlank String name,
        String type,
        String audience,
        Instant scheduledAt,

        // LinkedIn search targeting (project spec section 1)
        String industry,
        String designation,
        String location,
        Integer minExperience,
        Integer maxExperience,
        List<String> keywords,
        String companyName,
        String companySize,
        Integer minEmployeeCount,
        Integer maxEmployeeCount,
        List<String> technologies,
        String revenue,
        String customSearchUrl
) {}
