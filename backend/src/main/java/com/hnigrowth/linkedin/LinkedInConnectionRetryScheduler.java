package com.hnigrowth.linkedin;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.outreach.ApprovalStatus;
import com.hnigrowth.outreach.MessageRepository;
import com.hnigrowth.outreach.MessageService;
import com.hnigrowth.outreach.OutreachMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Retries LinkedIn messages that couldn't be sent because the prospect wasn't
 * a 1st-degree connection yet (LinkedInMessagingProvider auto-sent a
 * connection request and left the message APPROVED-but-unsent). Runs daily,
 * re-checking connection status and completing the send once accepted --
 * this is the "only send the message once connected" half of the requirement,
 * matching the automatic connection-request half already in
 * LinkedInMessagingProvider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInConnectionRetryScheduler {

    private final MessageRepository messageRepository;
    private final MessageService messageService;

    @Scheduled(cron = "0 0 10 * * *") // 10:00 daily
    @Transactional
    public void retryPendingConnections() {
        List<OutreachMessage> stuck = messageRepository.findByStatusOrderByCreatedAtDesc(ApprovalStatus.APPROVED)
                .stream()
                .filter(m -> m.getChannel() == Channel.LINKEDIN)
                .filter(m -> m.getLastError() != null && m.getLastError().toLowerCase().contains("connect"))
                .toList();

        int retried = 0;
        for (OutreachMessage m : stuck) {
            try {
                messageService.sendApproved(m.getId());
                retried++;
            } catch (Exception e) {
                // Still not connected (or another transient failure) -- leave it for
                // tomorrow's run rather than escalating; lastError is already updated
                // by sendApproved itself.
                log.debug("[linkedin-retry] message {} still not sendable: {}", m.getId(), e.getMessage());
            }
        }
        if (!stuck.isEmpty()) {
            log.info("[linkedin-retry] retried {} messages waiting on LinkedIn connection, {} now sent",
                    stuck.size(), retried);
        }
    }
}
