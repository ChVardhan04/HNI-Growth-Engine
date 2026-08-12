package com.hnigrowth.linkedin.dto;

import java.util.List;

/** Raw profile payload as returned by the LinkedIn/Unipile search & profile endpoints. */
public record LinkedInProfileDto(
        String providerProfileId,
        String name,
        String headline,
        String company,
        String currentPosition,
        String location,
        Integer yearsExperience,
        List<String> education,
        List<String> skills,
        String profileUrl,
        String profileImageUrl,
        String industry,
        String companySize,
        String connectionDegree,
        String about
) {}
