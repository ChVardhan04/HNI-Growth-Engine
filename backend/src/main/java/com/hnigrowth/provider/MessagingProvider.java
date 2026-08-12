package com.hnigrowth.provider;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.outreach.OutreachMessage;

/**
 * Abstraction over outbound message dispatch across channels. Implementations
 * are responsible for validating that they have what they need to actually
 * deliver (a connection on LinkedIn, an email address, a phone number, etc.)
 * and returning a clear failure rather than silently pretending to send.
 */
public interface MessagingProvider {
    boolean supports(Channel channel);

    /** The full Lead is passed (not just an id) so each provider can pull
     * whatever recipient info it needs (email, phone, LinkedIn profile id)
     * and validate it's present before attempting to send. */
    MessageSendResult send(OutreachMessage message, Lead lead);
}
