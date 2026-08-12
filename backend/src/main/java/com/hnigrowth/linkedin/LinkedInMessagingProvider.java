package com.hnigrowth.linkedin;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.linkedin.dto.ConnectionStatus;
import com.hnigrowth.linkedin.dto.LinkedInMessageRequest;
import com.hnigrowth.linkedin.dto.LinkedInSendResult;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.outreach.OutreachMessage;
import com.hnigrowth.provider.MessageSendResult;
import com.hnigrowth.provider.MessagingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Dispatches OutreachMessages of channel LINKEDIN.
 *
 * Core rule (per project requirement): LinkedIn does not allow a direct
 * message to someone who isn't a 1st-degree connection. So for any
 * FIRST_MESSAGE/FOLLOW_UP_* stage, this checks connection status BEFORE
 * attempting st.sendMessage:
 *   - CONNECTED     -> send the actual message
 *   - NOT_CONNECTED -> automatically send a connection request instead (using
 *                      the drafted message, trimmed, as the connection note),
 *                      and report failure so the RM-facing message stays
 *                      pending -- LinkedInConnectionRetryScheduler will retry
 *                      the real message automatically once accepted.
 *   - PENDING       -> a connection request is already out; just wait.
 *   - UNKNOWN       -> treated conservatively like NOT_CONNECTED (never risk
 *                      sending a message that LinkedIn will silently drop).
 * A CONNECTION_REQUEST-stage message is always just sent as a connection
 * request, no status check needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInMessagingProvider implements MessagingProvider {

    private final LinkedInService linkedInService;

    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.LINKEDIN;
    }

    @Override
    public MessageSendResult send(OutreachMessage message, Lead lead) {
        String personUrl = lead.getProviderProfileId();
        if (personUrl == null || personUrl.isBlank()) {
            return MessageSendResult.failed(
                    "This lead has no LinkedIn profile URL on file -- cannot send via LinkedIn.");
        }

        if (message.getStage() == MessageStage.CONNECTION_REQUEST) {
            LinkedInSendResult result = linkedInService.send(new LinkedInMessageRequest(
                    personUrl, message.getBody(), LinkedInMessageRequest.LinkedInMessageType.CONNECTION_REQUEST));
            return toGeneric(result);
        }

        // FIRST_MESSAGE / FOLLOW_UP_*: connection required first.
        ConnectionStatus status = linkedInService.checkConnectionStatus(personUrl);
        lead.setConnectionStatus(status.name());

        return switch (status) {
            case CONNECTED -> toGeneric(linkedInService.send(new LinkedInMessageRequest(
                    personUrl, message.getBody(), LinkedInMessageRequest.LinkedInMessageType.DIRECT_MESSAGE)));
            case PENDING -> MessageSendResult.failed(
                    "Not connected yet -- a connection request is already pending acceptance. "
                            + "This message will be retried automatically once they accept.");
            case NOT_CONNECTED, UNKNOWN -> autoSendConnectionRequest(message, personUrl);
        };
    }

    private MessageSendResult autoSendConnectionRequest(OutreachMessage message, String personUrl) {
        String note = message.getBody() == null ? "" : message.getBody();
        if (note.length() > 280) note = note.substring(0, 277) + "..."; // LinkedIn connection notes are short
        try {
            LinkedInSendResult connectResult = linkedInService.send(new LinkedInMessageRequest(
                    personUrl, note, LinkedInMessageRequest.LinkedInMessageType.CONNECTION_REQUEST));
            if (connectResult.success()) {
                return MessageSendResult.failed(
                        "Not connected on LinkedIn yet -- sent a connection request automatically. "
                                + "This message will be sent automatically once they accept.");
            }
            return MessageSendResult.failed(
                    "Not connected, and the automatic connection request also failed: " + connectResult.errorMessage());
        } catch (Exception e) {
            log.warn("[linkedin] auto connection-request failed for {}: {}", personUrl, e.getMessage());
            return MessageSendResult.failed(
                    "Not connected on LinkedIn, and the automatic connection request failed: " + e.getMessage());
        }
    }

    private MessageSendResult toGeneric(LinkedInSendResult r) {
        return r.success() ? MessageSendResult.ok(r.providerMessageId(), r.status())
                : MessageSendResult.failed(r.errorMessage());
    }
}
