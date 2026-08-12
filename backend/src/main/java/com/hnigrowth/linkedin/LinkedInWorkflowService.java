package com.hnigrowth.linkedin;

import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import com.hnigrowth.campaign.Campaign;
import com.hnigrowth.enrichment.EnrichmentService;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.lead.LeadService;
import com.hnigrowth.lead.LeadSource;
import com.hnigrowth.lead.dto.LeadResponse;
import com.hnigrowth.linkedin.dto.LinkedInProfileDto;
import com.hnigrowth.linkedin.dto.LinkedInSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrates the "start campaign" automation: LinkedIn search -> enrichment
 * -> store as leads -> AI scoring (delegated to LeadService.importFromLinkedIn).
 * This is the glue between the LinkedIn integration module and the rest of
 * the platform, kept out of the Controller per the clean-architecture rule.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkedInWorkflowService {

    private final LinkedInService linkedInService;
    private final EnrichmentService enrichmentService;
    private final LeadService leadService;
    private final AuditService auditService;

    public List<LeadResponse> runCampaignSearch(Campaign campaign) {
        LinkedInSearchRequest request = new LinkedInSearchRequest(
                campaign.getIndustry(), campaign.getDesignation(), campaign.getLocation(),
                campaign.getMinExperience(), campaign.getMaxExperience(),
                campaign.getKeywords(), campaign.getCompanyName(), campaign.getCompanySize(),
                campaign.getMinEmployeeCount(), campaign.getMaxEmployeeCount(),
                campaign.getTechnologies(), campaign.getRevenue(), 25, campaign.getCustomSearchUrl()
        );

        List<LinkedInProfileDto> profiles = linkedInService.search(request);
        auditService.log(AuditAction.CRM, "Campaign", campaign.getId(),
                "LinkedIn search returned " + profiles.size() + " profiles");

        List<LeadResponse> imported = profiles.stream()
                .map(p -> {
                    Lead lead = enrichmentService.enrichFromLinkedIn(p);
                    lead.setSource(LeadSource.LINKEDIN);
                    lead.setCampaignId(campaign.getId());
                    return leadService.importEnrichedLead(lead, campaign.getOwner());
                })
                .toList();

        auditService.log(AuditAction.CRM, "Campaign", campaign.getId(),
                imported.size() + " leads imported, enriched and AI-scored");
        log.info("[linkedin-workflow] campaign {} imported {} leads", campaign.getId(), imported.size());
        return imported;
    }
}
