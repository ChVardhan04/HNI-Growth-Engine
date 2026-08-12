package com.hnigrowth.linkedin.dto;

import java.util.List;

/** Maps 1:1 to campaign targeting fields (project spec section 1). */
public record LinkedInSearchRequest(
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
        Integer maxResults,
        /** Optional: a search results URL copied from LinkedIn/Sales Navigator after
         * configuring filters there directly. When set, this overrides term/filter
         * entirely on the Linked API side and is the most reliable way to target a
         * search, since it avoids guessing at Linked API's strict filter enums
         * (e.g. "industries" requires exact LinkedIn taxonomy values). */
        String customSearchUrl
) {}
