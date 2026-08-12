package com.hnigrowth.dashboard;

import com.hnigrowth.audit.AuditRepository;
import com.hnigrowth.campaign.CampaignRepository;
import com.hnigrowth.campaign.CampaignStatus;
import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.LeadMapper;
import com.hnigrowth.lead.LeadRepository;
import com.hnigrowth.lead.LeadStatus;
import com.hnigrowth.lead.Tier;
import com.hnigrowth.outreach.ApprovalStatus;
import com.hnigrowth.outreach.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    /** Illustrative average revenue per converted HNI client (config in real system). */
    private static final double AVG_REVENUE_PER_CONVERSION = 250_000d;

    private final LeadRepository leadRepository;
    private final MessageRepository messageRepository;
    private final CampaignRepository campaignRepository;
    private final AuditRepository auditRepository;
    private final LeadMapper leadMapper;

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        long total = leadRepository.count();
        long qualified = leadRepository.countByStatus(LeadStatus.QUALIFIED);
        long salesReady = leadRepository.countByStatus(LeadStatus.SALES_READY);
        long converted = leadRepository.countByStatus(LeadStatus.CONVERTED);
        long hot = leadRepository.countByIntentBand(IntentBand.HOT);
        long cold = leadRepository.countByIntentBand(IntentBand.COLD);
        long pendingApprovals = messageRepository.countByStatus(ApprovalStatus.PENDING_APPROVAL);
        long activeCampaigns = campaignRepository.countByStatus(CampaignStatus.ACTIVE);

        double conversionRate = total == 0 ? 0 : round(100.0 * converted / total);
        // Simple predictive heuristic: sales-ready + hot leads likely to convert.
        double predicted = total == 0 ? 0
                : round(conversionRate + 100.0 * (salesReady + hot) / total * 0.35);
        double revenueForecast = (converted + (salesReady + hot) * 0.35) * AVG_REVENUE_PER_CONVERSION;

        Map<String, Long> byTier = new LinkedHashMap<>();
        for (Tier t : Tier.values()) byTier.put(t.name(), leadRepository.countByTier(t));

        Map<String, Long> byBand = new LinkedHashMap<>();
        for (IntentBand b : IntentBand.values()) byBand.put(b.name(), leadRepository.countByIntentBand(b));

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (LeadStatus s : LeadStatus.values()) byStatus.put(s.name(), leadRepository.countByStatus(s));

        var topProspects = leadRepository.findTop5ByOrderByIcpScoreDesc()
                .stream().map(leadMapper::toResponse).toList();
        var recentAudit = auditRepository.findTop10ByOrderByCreatedAtDesc();

        return new DashboardStats(total, qualified, salesReady, converted, hot, cold,
                pendingApprovals, activeCampaigns, conversionRate, Math.min(100, predicted),
                revenueForecast, byTier, byBand, byStatus, topProspects, recentAudit);
    }

    private double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
