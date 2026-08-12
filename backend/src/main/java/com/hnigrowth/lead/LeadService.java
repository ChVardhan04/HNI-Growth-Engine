package com.hnigrowth.lead;

import com.hnigrowth.advisor.AdvisorRoutingService;
import com.hnigrowth.ai.AIService;
import com.hnigrowth.ai.model.Recommendation;
import com.hnigrowth.ai.model.ScoringResult;
import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import com.hnigrowth.common.exception.ResourceNotFoundException;
import com.hnigrowth.lead.dto.LeadRequest;
import com.hnigrowth.lead.dto.LeadResponse;
import com.hnigrowth.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper mapper;
    private final AIService aiService;
    private final AdvisorRoutingService advisorRoutingService;
    private final AuditService auditService;

    /**
     * Creates a lead and runs the AI pipeline: ICP scoring -> tier -> reason ->
     * next best action -> advisor routing. This is the "Lead Created" step of the
     * AI workflow.
     */
    @Transactional
    public LeadResponse create(LeadRequest request, String owner) {
        Lead lead = mapper.toEntity(request);
        lead.setOwner(owner);

        applyIcpScoring(lead);
        applyRecommendation(lead);
        routeAdvisor(lead);

        Lead saved = leadRepository.save(lead);
        auditService.log(AuditAction.CREATE, "Lead", saved.getId(),
                "Lead created and AI-scored: ICP=" + saved.getIcpScore() + " tier=" + saved.getTier());
        auditService.log(AuditAction.AI_PROCESSING, "Lead", saved.getId(),
                "ICP reason: " + saved.getIcpReason());
        return mapper.toResponse(saved);
    }

    /**
     * Persists an already-enriched Lead (e.g. from the LinkedIn workflow) and
     * runs the same AI pipeline as manual creation: ICP scoring -> tier ->
     * next best action -> advisor routing.
     */
    @Transactional
    public LeadResponse importEnrichedLead(Lead lead, String owner) {
        lead.setOwner(owner);
        if (lead.getPriority() == null) lead.setPriority(LeadPriority.MEDIUM);
        if (lead.getStatus() == null) lead.setStatus(LeadStatus.NEW);

        applyIcpScoring(lead);
        applyRecommendation(lead);
        routeAdvisor(lead);

        Lead saved = leadRepository.save(lead);
        auditService.log(AuditAction.CREATE, "Lead", saved.getId(),
                "Lead imported from LinkedIn and AI-scored: ICP=" + saved.getIcpScore() + " tier=" + saved.getTier());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeadResponse get(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> list(String search, Pageable pageable) {
        Page<Lead> page = (search == null || search.isBlank())
                ? leadRepository.findAll(pageable)
                : leadRepository
                .findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search, search, search, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional
    public LeadResponse update(Long id, LeadRequest request) {
        Lead lead = findOrThrow(id);
        mapper.updateEntity(lead, request);
        // Re-score because profile attributes changed.
        applyIcpScoring(lead);
        applyRecommendation(lead);
        auditService.log(AuditAction.UPDATE, "Lead", id, "Lead updated and re-scored");
        return mapper.toResponse(leadRepository.save(lead));
    }

    @Transactional
    public void delete(Long id) {
        Lead lead = findOrThrow(id);
        leadRepository.delete(lead);
        auditService.log(AuditAction.DELETE, "Lead", id, "Lead deleted");
    }

    @Transactional
    public LeadResponse rescore(Long id) {
        Lead lead = findOrThrow(id);
        applyIcpScoring(lead);
        applyRecommendation(lead);
        auditService.log(AuditAction.AI_PROCESSING, "Lead", id, "Manual re-score requested");
        return mapper.toResponse(leadRepository.save(lead));
    }

    // --- internal AI orchestration ---

    private void applyIcpScoring(Lead lead) {
        ScoringResult result = aiService.scoreLead(lead);
        lead.setIcpScore(result.score());
        lead.setTier(result.tier());
        lead.setIcpReason(result.reason());
        lead.setIcpConfidence(result.confidence());
        if (lead.getStatus() == LeadStatus.NEW) {
            lead.setStatus(LeadStatus.QUALIFIED);
        }
    }

    private void applyRecommendation(Lead lead) {
        Recommendation rec = aiService.recommend(lead, lead.getIntentScore(), lead.getIntentBand());
        lead.setNextBestAction(rec.action());
        lead.setRecommendationReason(rec.reasoning());
    }

    private void routeAdvisor(Lead lead) {
        Optional<User> rm = advisorRoutingService.routeLead(lead);
        rm.ifPresent(user -> {
            lead.setAssignedRmId(user.getId());
            lead.setAssignedRmName(user.getFullName());
        });
    }

    Lead findOrThrow(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
    }
}
