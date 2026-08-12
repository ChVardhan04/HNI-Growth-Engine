package com.hnigrowth.campaign;

import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import com.hnigrowth.campaign.dto.CampaignRequest;
import com.hnigrowth.common.exception.BadRequestException;
import com.hnigrowth.common.exception.ResourceNotFoundException;
import com.hnigrowth.lead.dto.LeadResponse;
import com.hnigrowth.linkedin.LinkedInWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final AuditService auditService;
    private final LinkedInWorkflowService linkedInWorkflowService;
    private final CampaignFailureRecorder failureRecorder;

    @Transactional
    public Campaign create(CampaignRequest req, String owner) {
        Campaign c = Campaign.builder()
                .name(req.name()).type(req.type()).audience(req.audience())
                .owner(owner)
                .scheduledAt(req.scheduledAt())
                .status(req.scheduledAt() != null ? CampaignStatus.SCHEDULED : CampaignStatus.DRAFT)
                .industry(req.industry()).designation(req.designation()).location(req.location())
                .minExperience(req.minExperience()).maxExperience(req.maxExperience())
                .keywords(req.keywords() == null ? List.of() : req.keywords())
                .companyName(req.companyName()).companySize(req.companySize())
                .minEmployeeCount(req.minEmployeeCount()).maxEmployeeCount(req.maxEmployeeCount())
                .technologies(req.technologies() == null ? List.of() : req.technologies())
                .revenue(req.revenue())
                .customSearchUrl(req.customSearchUrl())
                .build();
        applyAiRecommendation(c);
        c = campaignRepository.save(c);
        auditService.log(AuditAction.CAMPAIGN, "Campaign", c.getId(), "Campaign created: " + c.getName());
        return c;
    }

    /**
     * Starts the campaign: automatically runs LinkedIn search -> enrichment ->
     * lead storage -> AI scoring, with no manual import step (project spec
     * section 1). This is the automation entry point invoked from the
     * Campaign Wizard's "Launch Campaign" action.
     */
    @Transactional
    public List<LeadResponse> start(Long id) {
        Campaign c = get(id);
        if (c.getStatus() == CampaignStatus.ACTIVE || c.getStatus() == CampaignStatus.COMPLETED) {
            throw new BadRequestException("Campaign has already been started");
        }
        c.setStatus(CampaignStatus.ACTIVE);
        c.setStartedAt(Instant.now());
        c.setFailureReason(null);
        campaignRepository.save(c);
        auditService.log(AuditAction.CAMPAIGN, "Campaign", id, "Campaign started - LinkedIn search launching automatically");

        List<LeadResponse> imported;
        try {
            imported = linkedInWorkflowService.runCampaignSearch(c);
        } catch (Exception e) {
            // A real LinkedIn/Linked API failure -- mark the campaign FAILED with the
            // actual reason visible to the RM, rather than quietly reporting success
            // with zero (or worse, fabricated mock) leads. Persisted in its own
            // transaction (see CampaignFailureRecorder) since the exception thrown
            // below would otherwise roll back this save along with everything else
            // in the current @Transactional method.
            failureRecorder.recordFailure(id, e.getMessage());
            throw new BadRequestException("LinkedIn search failed: " + e.getMessage());
        }

        c.setLeadsImported(imported.size());
        c.setStatus(CampaignStatus.COMPLETED);
        campaignRepository.save(c);
        return imported;
    }

    @Transactional(readOnly = true)
    public List<Campaign> list() { return campaignRepository.findAll(); }

    @Transactional(readOnly = true)
    public Campaign get(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", id));
    }

    @Transactional
    public Campaign updateStatus(Long id, CampaignStatus status) {
        Campaign c = get(id);
        c.setStatus(status);
        auditService.log(AuditAction.CAMPAIGN, "Campaign", id, "Status -> " + status);
        return campaignRepository.save(c);
    }

    @Transactional
    public void delete(Long id) {
        campaignRepository.deleteById(id);
        auditService.log(AuditAction.CAMPAIGN, "Campaign", id, "Campaign deleted");
    }

    /**
     * AI campaign recommendation (best time / audience / expected conversion).
     * Rule-based placeholder; swap for an LLM/model-backed recommender later.
     */
    private void applyAiRecommendation(Campaign c) {
        c.setRecommendedBestTime("Tuesday 10:00-11:00 local time");
        c.setRecommendedAudience(c.getAudience() == null ? "Tier A/A+ HNIs in metro regions" : c.getAudience());
        c.setExpectedConversionRate(12);
    }
}
