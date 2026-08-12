package com.hnigrowth.outreach.dto;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import jakarta.validation.constraints.NotNull;

public record GenerateMessageRequest(
        @NotNull Long leadId,
        @NotNull Channel channel,
        MessageStage stage,
        MessageLength length
) {}
