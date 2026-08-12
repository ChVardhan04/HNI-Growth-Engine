package com.hnigrowth.lead;

import com.hnigrowth.lead.dto.LeadRequest;
import com.hnigrowth.lead.dto.LeadResponse;
import org.springframework.stereotype.Component;

/** Maps between Lead entities and DTOs. */
@Component
public class LeadMapper {

    public Lead toEntity(LeadRequest r) {
        return Lead.builder()
                .name(r.name())
                .email(r.email())
                .phone(r.phone())
                .company(r.company())
                .designation(r.designation())
                .industry(r.industry())
                .location(r.location())
                .yearsExperience(r.yearsExperience())
                .linkedinUrl(r.linkedinUrl())
                .source(r.source())
                .priority(r.priority() == null ? LeadPriority.MEDIUM : r.priority())
                .notes(r.notes())
                .previousEngagementScore(r.previousEngagementScore() == null ? 0 : r.previousEngagementScore())
                .build();
    }

    public void updateEntity(Lead lead, LeadRequest r) {
        lead.setName(r.name());
        lead.setEmail(r.email());
        lead.setPhone(r.phone());
        lead.setCompany(r.company());
        lead.setDesignation(r.designation());
        lead.setIndustry(r.industry());
        lead.setLocation(r.location());
        lead.setYearsExperience(r.yearsExperience());
        lead.setLinkedinUrl(r.linkedinUrl());
        lead.setSource(r.source());
        if (r.priority() != null) lead.setPriority(r.priority());
        lead.setNotes(r.notes());
        if (r.previousEngagementScore() != null) lead.setPreviousEngagementScore(r.previousEngagementScore());
    }

    public LeadResponse toResponse(Lead l) {
        return new LeadResponse(
                l.getId(), l.getName(), l.getEmail(), l.getPhone(), l.getCompany(),
                l.getDesignation(), l.getIndustry(), l.getLocation(), l.getYearsExperience(),
                l.getLinkedinUrl(), l.getSource(), l.getStatus(), l.getPriority(),
                l.getOwner(), l.getAssignedRmId(), l.getAssignedRmName(), l.getNotes(),
                l.getIcpScore(), l.getTier(), l.getIcpReason(), l.getIcpConfidence(),
                l.getIntentScore(), l.getIntentBand(), l.getNextBestAction(),
                l.getRecommendationReason(), l.getProfileImageUrl(), l.getCurrentPosition(),
                l.getCompanySize(), l.getConnectionDegree(), l.getConnectionStatus(), l.getSkills(), l.getEducation(),
                l.getCampaignId(), l.getCreatedAt(), l.getUpdatedAt());
    }
}
