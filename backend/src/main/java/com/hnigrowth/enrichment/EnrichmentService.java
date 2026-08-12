package com.hnigrowth.enrichment;

import com.hnigrowth.lead.Lead;
import com.hnigrowth.lead.LeadPriority;
import com.hnigrowth.linkedin.dto.LinkedInProfileDto;
import com.hnigrowth.provider.EnrichmentProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Separate enrichment stage (project spec section 4): takes a raw LinkedIn
 * profile (or an existing Lead) and fills in/normalizes derived fields before
 * the lead is persisted and scored. Kept independent of LeadService so extra
 * enrichment providers (Clearbit, ZoomInfo, etc.) can be added behind
 * EnrichmentProvider without touching lead persistence logic.
 */
@Service
public class EnrichmentService implements EnrichmentProvider {

    /** Builds a fully-populated (but not-yet-persisted) Lead from a raw LinkedIn profile. */
    public Lead enrichFromLinkedIn(LinkedInProfileDto p) {
        Lead lead = Lead.builder()
                .name(p.name())
                .company(p.company())
                .designation(p.currentPosition() != null ? p.currentPosition() : extractTitle(p.headline()))
                .industry(p.industry())
                .location(p.location())
                .yearsExperience(p.yearsExperience())
                .linkedinUrl(p.profileUrl())
                .profileImageUrl(p.profileImageUrl())
                .currentPosition(p.currentPosition())
                .companySize(p.companySize())
                .connectionDegree(p.connectionDegree())
                .providerProfileId(p.providerProfileId())
                .skills(p.skills() == null ? "" : String.join(", ", p.skills()))
                .education(p.education() == null ? "" : String.join(", ", p.education()))
                .notes(p.about() == null ? null : truncate(p.about(), 1000))
                .priority(LeadPriority.MEDIUM)
                .build();
        return enrich(lead);
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /** Normalizes/derives extra fields on an already-built lead. */
    @Override
    public Lead enrich(Lead lead) {
        if (lead.getIndustry() != null) {
            lead.setIndustry(capitalize(lead.getIndustry().trim()));
        }
        if (isDecisionMaker(lead.getDesignation())) {
            lead.setPriority(LeadPriority.HIGH);
        }
        return lead;
    }

    private boolean isDecisionMaker(String title) {
        if (title == null) return false;
        String t = title.toLowerCase();
        return List.of("ceo", "founder", "director", "cxo", "cfo", "cto", "coo", "managing director", "partner", "president", "chairman")
                .stream().anyMatch(t::contains);
    }

    private String extractTitle(String headline) {
        if (headline == null) return null;
        int at = headline.indexOf(" at ");
        return at > 0 ? headline.substring(0, at) : headline;
    }

    private String capitalize(String s) {
        if (s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
