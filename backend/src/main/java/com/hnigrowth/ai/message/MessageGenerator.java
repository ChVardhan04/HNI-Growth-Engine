package com.hnigrowth.ai.message;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.GeneratedMessage;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.lead.Lead;

/** Generates personalized, editable outreach for a channel. */
public interface MessageGenerator {
    GeneratedMessage generate(Lead lead, Channel channel);

    /** Stage- and length-aware generation (connection request / follow-ups / short-long variants). */
    default GeneratedMessage generate(Lead lead, Channel channel, MessageStage stage, MessageLength length) {
        return generate(lead, channel);
    }
}
