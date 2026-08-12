package com.hnigrowth.scheduler;

import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.lead.LeadRepository;
import com.hnigrowth.lead.LeadStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Background jobs (module 14). Uses Spring's scheduler. In production these
 * would push to an email queue / reminder service; here they perform safe,
 * self-contained maintenance so the schedule wiring is real and testable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformScheduler {

    private final LeadRepository leadRepository;

    /** Daily follow-up sweep: surface engaged leads that have gone quiet. */
    @Scheduled(cron = "0 0 9 * * *") // every day at 09:00
    public void dailyFollowUp() {
        List<Lead> engaged = leadRepository.findByStatus(LeadStatus.ENGAGED);
        log.info("[scheduler] Daily follow-up sweep: {} engaged leads to review", engaged.size());
    }

    /** Intent refresh: gently decay cold, stale leads so scores stay meaningful. */
    @Scheduled(fixedRateString = "PT6H") // every 6 hours
    @Transactional
    public void refreshIntent() {
        List<Lead> cold = leadRepository.findByStatus(LeadStatus.NURTURING);
        for (Lead lead : cold) {
            int decayed = Math.max(0, lead.getIntentScore() - 2);
            lead.setIntentScore(decayed);
            lead.setIntentBand(IntentBand.fromScore(decayed));
        }
        if (!cold.isEmpty()) {
            leadRepository.saveAll(cold);
            log.info("[scheduler] Intent refresh: decayed {} nurturing leads", cold.size());
        }
    }

    /** Campaign scheduler heartbeat (placeholder for launching due campaigns). */
    @Scheduled(fixedRateString = "PT1H")
    public void campaignScheduler() {
        log.debug("[scheduler] Campaign scheduler heartbeat");
    }

    /** CRM reminder job (placeholder for meeting reminders / follow-up tasks). */
    @Scheduled(cron = "0 0 8 * * *")
    public void crmReminders() {
        log.info("[scheduler] CRM reminder job executed");
    }
}
