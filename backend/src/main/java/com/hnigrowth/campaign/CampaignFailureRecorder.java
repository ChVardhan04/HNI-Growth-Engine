package com.hnigrowth.campaign;

import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists a campaign failure in its own transaction (REQUIRES_NEW), so that
 * when CampaignService.start() subsequently throws to report the error to
 * the caller, that exception's rollback doesn't also wipe out the FAILED
 * status + failureReason we just wrote. Kept as a separate bean rather than
 * a method on CampaignService because @Transactional on a self-invoked
 * method (this.foo()) is silently ignored by Spring's proxy-based AOP.
 */
@Service
@RequiredArgsConstructor
public class CampaignFailureRecorder {

    private final CampaignRepository campaignRepository;
    private final AuditService auditService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long campaignId, String reason) {
        campaignRepository.findById(campaignId).ifPresent(c -> {
            c.setStatus(CampaignStatus.FAILED);
            c.setFailureReason(reason);
            campaignRepository.save(c);
        });
        auditService.log(AuditAction.CAMPAIGN, "Campaign", campaignId, "Campaign FAILED: " + reason);
    }
}
