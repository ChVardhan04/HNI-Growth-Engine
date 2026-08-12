package com.hnigrowth.intent;

import com.hnigrowth.ai.AIService;
import com.hnigrowth.ai.model.Recommendation;
import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import com.hnigrowth.common.exception.ResourceNotFoundException;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.lead.LeadRepository;
import com.hnigrowth.lead.LeadService;
import com.hnigrowth.lead.LeadStatus;
import com.hnigrowth.notification.NotificationService;
import com.hnigrowth.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Intent Intelligence Engine. Every tracked activity updates the lead's intent
 * score and band, then re-runs the recommendation. When a lead becomes HOT it
 * is promoted to SALES_READY.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentService {

    private final ActivityRepository activityRepository;
    private final LeadRepository leadRepository;
    private final LeadService leadService;
    private final AIService aiService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    /** Intent score at/above this threshold triggers hot-lead RM notification (project spec section 15). */
    private static final int HOT_LEAD_THRESHOLD = 80;

    @Transactional
    public Lead trackActivity(Long leadId, ActivityType type, String description) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", leadId));

        // A LinkedIn connection acceptance changes ICP-relevant facts (connection
        // degree feeds the decision-maker bonus in RuleBasedIcpScoringEngine), not
        // just intent -- so rescore ICP too, not only the intent score below.
        if (type == ActivityType.CONNECTION_ACCEPTED) {
            lead.setConnectionDegree("1st");
            lead.setConnectionStatus("CONNECTED");
            leadRepository.save(lead);
            try {
                leadService.rescore(leadId);
                lead = leadRepository.findById(leadId).orElseThrow(() -> new ResourceNotFoundException("Lead", leadId));
            } catch (Exception e) {
                log.warn("[intent] ICP rescore after connection-accepted failed for lead {}: {}", leadId, e.getMessage());
            }
        }

        int previousScore = lead.getIntentScore();
        int newScore = Math.min(100, previousScore + type.getWeight());
        lead.setIntentScore(newScore);
        IntentBand band = IntentBand.fromScore(newScore);
        IntentBand previousBand = lead.getIntentBand();
        lead.setIntentBand(band);

        // Re-evaluate the next best action against the new intent.
        Recommendation rec = aiService.recommend(lead, newScore, band);
        lead.setNextBestAction(rec.action());
        lead.setRecommendationReason(rec.reasoning());

        // Lifecycle promotion.
        if (band == IntentBand.HOT && lead.getStatus() != LeadStatus.CONVERTED) {
            lead.setStatus(LeadStatus.SALES_READY);
        } else if (lead.getStatus() == LeadStatus.QUALIFIED) {
            lead.setStatus(LeadStatus.ENGAGED);
        }

        activityRepository.save(Activity.builder()
                .leadId(leadId).type(type).description(description)
                .weightApplied(type.getWeight()).build());
        leadRepository.save(lead);

        auditService.log(AuditAction.INTENT_UPDATE, "Lead", leadId,
                "Activity " + type + " (+" + type.getWeight() + ") -> intent " + newScore + " [" + band + "]");

        // Hot Lead Detection (project spec section 15): notify RM the moment
        // the lead crosses the threshold, but only once per crossing.
        boolean justCrossedThreshold = newScore >= HOT_LEAD_THRESHOLD && previousScore < HOT_LEAD_THRESHOLD;
        boolean justBecameHot = band == IntentBand.HOT && previousBand != IntentBand.HOT;
        if ((justCrossedThreshold || justBecameHot) && lead.getAssignedRmId() != null) {
            notificationService.notify(
                    lead.getAssignedRmId(),
                    "Hot lead: " + lead.getName(),
                    lead.getName() + " (" + (lead.getDesignation() == null ? "lead" : lead.getDesignation())
                            + " at " + (lead.getCompany() == null ? "unknown company" : lead.getCompany())
                            + ") just crossed intent score " + newScore + " and is now HOT. "
                            + rec.reasoning(),
                    "/leads/" + leadId,
                    NotificationType.HOT_LEAD);
            auditService.log(AuditAction.INTENT_UPDATE, "Lead", leadId,
                    "Hot lead detected (intent=" + newScore + ") -> RM " + lead.getAssignedRmId() + " notified");
        }

        return lead;
    }

    @Transactional(readOnly = true)
    public List<Activity> timeline(Long leadId) {
        return activityRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
    }
}
