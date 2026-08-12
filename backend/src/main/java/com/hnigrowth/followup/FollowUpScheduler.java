package com.hnigrowth.followup;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.audit.AuditAction;
import com.hnigrowth.audit.AuditService;
import com.hnigrowth.intent.Activity;
import com.hnigrowth.intent.ActivityRepository;
import com.hnigrowth.intent.ActivityType;
import com.hnigrowth.outreach.MessageSentEvent;
import com.hnigrowth.outreach.MessageService;
import com.hnigrowth.outreach.OutreachMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Follow-up sequencing (project spec section 14): Day 2 / 5 / 9 / 15 after the
 * initial outreach message is sent, unless the lead has replied.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpScheduler {

    private static final int[] DAY_OFFSETS = {2, 5, 9, 15};

    private final FollowUpRepository followUpRepository;
    private final ActivityRepository activityRepository;
    private final MessageService messageService;
    private final AuditService auditService;

    /** Reacts to a successful send by scheduling the Day 2/5/9/15 sequence,
     * but only for the initial outreach (not for follow-ups themselves, to
     * avoid re-scheduling chains on top of chains). */
    @EventListener
    @Transactional
    public void onMessageSent(MessageSentEvent event) {
        OutreachMessage sent = event.message();
        if (sent.getStage() == com.hnigrowth.ai.model.MessageStage.FIRST_MESSAGE
                || sent.getStage() == com.hnigrowth.ai.model.MessageStage.CONNECTION_REQUEST) {
            scheduleSequenceFor(sent);
        }
    }

    /** Called once the initial message is marked SENT. */
    @Transactional
    public void scheduleSequenceFor(OutreachMessage sentMessage) {
        Instant base = sentMessage.getSentAt() != null ? sentMessage.getSentAt() : Instant.now();
        for (int i = 0; i < DAY_OFFSETS.length; i++) {
            followUpRepository.save(FollowUp.builder()
                    .leadId(sentMessage.getLeadId())
                    .originalMessageId(sentMessage.getId())
                    .sequenceNumber(i + 1)
                    .dayOffset(DAY_OFFSETS[i])
                    .scheduledAt(base.plus(DAY_OFFSETS[i], ChronoUnit.DAYS))
                    .build());
        }
        auditService.log(AuditAction.CRM, "Lead", sentMessage.getLeadId(),
                "Follow-up sequence scheduled (Day 2/5/9/15)");
    }

    /** Runs daily: dispatch any due follow-ups, skipping leads that replied. */
    @Scheduled(cron = "0 30 9 * * *") // 09:30 daily, after the general follow-up sweep
    @Transactional
    public void dispatchDueFollowUps() {
        List<FollowUp> due = followUpRepository.findByStatusAndScheduledAtBefore(FollowUpStatus.SCHEDULED, Instant.now());
        for (FollowUp f : due) {
            if (hasReplied(f.getLeadId())) {
                f.setStatus(FollowUpStatus.SKIPPED_REPLIED);
                followUpRepository.save(f);
                continue;
            }
            try {
                var stage = switch (f.getSequenceNumber()) {
                    case 1 -> com.hnigrowth.ai.model.MessageStage.FOLLOW_UP_1;
                    case 2 -> com.hnigrowth.ai.model.MessageStage.FOLLOW_UP_2;
                    default -> com.hnigrowth.ai.model.MessageStage.FOLLOW_UP_3;
                };
                OutreachMessage draft = messageService.generate(f.getLeadId(), Channel.LINKEDIN, stage, com.hnigrowth.ai.model.MessageLength.SHORT);
                f.setGeneratedMessageId(draft.getId());
                f.setStatus(FollowUpStatus.SENT);
                followUpRepository.save(f);
                auditService.log(AuditAction.CRM, "Lead", f.getLeadId(),
                        "Follow-up #" + f.getSequenceNumber() + " (Day " + f.getDayOffset() + ") drafted for RM approval");
            } catch (Exception e) {
                f.setStatus(FollowUpStatus.FAILED);
                followUpRepository.save(f);
                log.warn("[followup] failed for lead {}: {}", f.getLeadId(), e.getMessage());
            }
        }
        if (!due.isEmpty()) log.info("[followup] processed {} due follow-ups", due.size());
    }

    private boolean hasReplied(Long leadId) {
        List<Activity> activities = activityRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
        return activities.stream().anyMatch(a -> a.getType() == ActivityType.REPLIED);
    }
}
